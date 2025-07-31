import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../api/authStore";
import { useEffect, useRef, useCallback, useState } from "react";

const CANVAS_WIDTH = 1024;
const CANVAS_HEIGHT = 1024;
const WS_URL = "ws://localhost:8081/api/ws";

const PALETTE_ABGR: number[] = [
    0xff000000, // Black 
    0xffffffff, // White
    0xff0000ff, // Red
    0xff00ff00, // Green
    0xffff0000, // Blue
    0xff00ffff, // Yellow
    0xffffff00, // Cyan
    0xffff00ff, // Magenta
    0xff888888, // Gray
    0xff0088ff, // Orange
    0xffff8800, // Light Blue
    0xff8800ff, // Purple
    0xff44ff44, // Light Green
    0xff4444ff, // Light Blue
    0xffff4444, // Light Red
    0xff222222, // Dark Gray
];

function abgrToRgbaStrings(abgrArray: number[]): string[] {
    return abgrArray.map(abgr => {
        const r = (abgr & 0xFF).toString(16).padStart(2, "0");
        const g = ((abgr >> 8) & 0xFF).toString(16).padStart(2, "0");
        const b = ((abgr >> 16) & 0xFF).toString(16).padStart(2, "0");
        return `#${r}${g}${b}`;
    });
}

const PALETTE_RGB = abgrToRgbaStrings(PALETTE_ABGR);

function HomePage() {
    const navigate = useNavigate();
    const username = useAuthStore((state) => state.username);
    const token = useAuthStore((state) => state.token);

    const wsRef = useRef<WebSocket | null>(null);
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const wrapperRef = useRef<HTMLDivElement>(null);
    const pixelsRef = useRef<Uint32Array | null>(null);
    const frameRef = useRef<number>(0);
    const pendingUpdatesRef = useRef<{ x: number, y: number, color: number }[]>([]);

    // UI State
    const [scale, setScale] = useState<number>(1);
    const [position, setPosition] = useState({ x: 0, y: 0 });
    const [selectedColor, setSelectedColor] = useState<number>(6);

    // Initialize pixel buffer
    const initPixelBuffer = useCallback(() => {
        if (!canvasRef.current) return;
        const ctx = canvasRef.current.getContext('2d', { willReadFrequently: true });
        if (!ctx) return;
        const imageData = ctx.createImageData(CANVAS_WIDTH, CANVAS_HEIGHT);
        pixelsRef.current = new Uint32Array(imageData.data.buffer);
        ctx.putImageData(imageData, 0, 0);
    }, []);

    // Batch render updates
    const renderUpdates = useCallback(() => {
        if (!canvasRef.current || !pixelsRef.current || pendingUpdatesRef.current.length === 0) {
            frameRef.current = requestAnimationFrame(renderUpdates);
            return;
        }

        const ctx = canvasRef.current.getContext('2d');
        if (!ctx) return;

        const updates = pendingUpdatesRef.current;
        pendingUpdatesRef.current = [];
        const imageData = ctx.getImageData(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        const pixels = new Uint32Array(imageData.data.buffer);

        for (const { x, y, color } of updates) {
            pixels[y * CANVAS_WIDTH + x] = PALETTE_ABGR[color];
        }

        ctx.putImageData(imageData, 0, 0);
        frameRef.current = requestAnimationFrame(renderUpdates);
    }, []);

    // Process binary data
    const processBinaryData = useCallback((data: ArrayBuffer) => {
        if (!canvasRef.current) return;
        const ctx = canvasRef.current.getContext('2d');
        if (!ctx || !pixelsRef.current) return;

        const packed = new Uint8Array(data);
        const imageData = ctx.createImageData(CANVAS_WIDTH, CANVAS_HEIGHT);
        const pixels = new Uint32Array(imageData.data.buffer);

        for (let i = 0; i < packed.length; i++) {
            const byte = packed[i];
            const idx = i * 2;
            if (idx < pixels.length) pixels[idx] = PALETTE_ABGR[(byte >> 4) & 0x0F];
            if (idx + 1 < pixels.length) pixels[idx + 1] = PALETTE_ABGR[byte & 0x0F];
        }

        ctx.putImageData(imageData, 0, 0);
        pixelsRef.current = pixels;
    }, []);

    // Handle pixel updates
    const handlePixelUpdate = useCallback((x: number, y: number, color: number) => {
        pendingUpdatesRef.current.push({ x, y, color });
    }, []);

    // WebSocket connection
    useEffect(() => {
        if (!username) {
            navigate("/auth");
            return;
        }

        initPixelBuffer();
        frameRef.current = requestAnimationFrame(renderUpdates);

        const ws = new WebSocket(WS_URL, ["binary", token]);
        ws.binaryType = "arraybuffer";

        ws.onopen = () => console.log("WebSocket connected");
        ws.onmessage = (event) => {
            if (event.data instanceof ArrayBuffer) {
                processBinaryData(event.data);
            } else if (typeof event.data === "string") {
                try {
                    const msg = JSON.parse(event.data);
                    if (msg.type === "pixel") {
                        handlePixelUpdate(msg.x, msg.y, msg.color);
                    }
                } catch (e) {
                    console.error("Failed to parse message:", event.data, e);
                }
            }
        };
        ws.onerror = (error) => console.error("WebSocket error:", error);
        ws.onclose = (event) => {
            if (event.code === 1008) navigate("/auth");
        };

        wsRef.current = ws;
        return () => {
            cancelAnimationFrame(frameRef.current);
            if (wsRef.current?.readyState === WebSocket.OPEN) {
                wsRef.current.close(1000, "Component unmounted");
            }
        };
    }, [username, token, navigate, initPixelBuffer, renderUpdates, processBinaryData, handlePixelUpdate]);

    // zoom via mouse wheel
    const handleWheel = useCallback((e: React.WheelEvent) => {
        e.preventDefault();
        if (!wrapperRef.current) return;

        const { left, top } = wrapperRef.current.getBoundingClientRect();
        const mx = e.clientX - left;
        const my = e.clientY - top;

        const deltaScale = e.deltaY < 0 ? 1.1 : 1 / 1.1;
        const newScale = Math.min(10, Math.max(1, scale * deltaScale));

        // move pos so that (mx,my) remains under the cursor
        setPosition(({ x, y }) => ({
            x: x - (mx / scale) * (newScale - scale),
            y: y - (my / scale) * (newScale - scale),
        }));
        setScale(newScale);
    }, [scale]);

    // WASD pan 
    useEffect(() => {
        const onKey = (e: KeyboardEvent) => {
            const step = 50 / scale;
            setPosition(p => {
                switch (e.key.toLowerCase()) {
                    case "w": return { x: p.x, y: p.y + step };
                    case "s": return { x: p.x, y: p.y - step };
                    case "a": return { x: p.x + step, y: p.y };
                    case "d": return { x: p.x - step, y: p.y };
                    default: return p;
                }
            });
        };
        window.addEventListener("keydown", onKey);
        return () => window.removeEventListener("keydown", onKey);
    }, [scale]);

    const handleClick = useCallback((e: React.MouseEvent) => {
        if (!canvasRef.current || !wrapperRef.current || !wsRef.current) return;

        const rect = wrapperRef.current.getBoundingClientRect();
        const mx = e.clientX - rect.left;
        const my = e.clientY - rect.top;

        // inverse of transform-origin 0,0 [ translate(tx,ty) scale(s) ]
        const s = scale;
        const tx = position.x;
        const ty = position.y;

        const x = Math.floor((mx - tx) / s + position.x / scale);
        const y = Math.floor((my - ty) / s + position.y / scale);

        console.log(`${x}, ${y}`);
        if (x >= 0 && x < CANVAS_WIDTH && y >= 0 && y < CANVAS_HEIGHT) {
            wsRef.current.send(JSON.stringify({ x, y, color: selectedColor }));
        }
    }, [position.x, position.y, scale, selectedColor]);

    return (
        <>
            <p>
                X: {position.x}

            </p>
            <p>
                Y: {position.y}
            </p>
            <p>
                Scale: {scale}
            </p>
            <div className="flex h-screen w-screen bg-gray-100">
                {/* Left sidebar - Color Palette */}
                <div className="w-16 h-min bg-white shadow-md p-2 flex flex-col items-center">
                    <div className="grid grid-cols-1 gap-2">
                        {PALETTE_RGB.map((color, index) => (
                            <button
                                key={index}
                                className={`w-10 h-10 rounded border-2 transition-all ${selectedColor === index
                                    ? 'border-blue-500 scale-110'
                                    : 'border-gray-300 hover:border-blue-300'
                                    }`}
                                style={{ backgroundColor: color }}
                                onClick={() => setSelectedColor(index)}
                                title={`Color ${index}`}
                            />
                        ))}
                    </div>
                </div>

                <div className="flex-1 relative overflow-hidden">
                    <div
                        ref={wrapperRef}
                        className="absolute top-0 left-0"
                        onWheel={handleWheel}
                        style={{
                            transformOrigin: "0 0",
                            transform: `translate(${position.x}px,${position.y}px) scale(${scale})`,
                            willChange: "transform",
                        }}
                    >
                        <canvas
                            ref={canvasRef}
                            width={CANVAS_WIDTH}
                            height={CANVAS_HEIGHT}
                            onClick={handleClick}
                            className="block bg-white"
                            style={{ imageRendering: "pixelated" }}
                        />
                    </div>
                </div>
            </div>
        </>
    );
}

export default HomePage;