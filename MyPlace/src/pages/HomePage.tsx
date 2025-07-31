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

    // Zoom handling with proper centering
    const handleWheel = (e: React.WheelEvent<HTMLDivElement>) => {
        e.preventDefault();
        const container = e.currentTarget;
        const rect = container.getBoundingClientRect();

        // Get mouse position relative to container center
        const containerCenterX = rect.width / 2;
        const containerCenterY = rect.height / 2;
        const mouseX = e.clientX - rect.left;
        const mouseY = e.clientY - rect.top;

        // Calculate mouse offset from center
        const offsetX = mouseX - containerCenterX;
        const offsetY = mouseY - containerCenterY;

        // Calculate new scale
        const delta = e.deltaY;
        const newScale = delta < 0
            ? Math.min(10, scale * 1.1)
            : Math.max(0.5, scale / 1.1);

        // Adjust position to zoom toward mouse
        setPosition(prev => ({
            x: prev.x - (offsetX * (newScale - scale) / scale),
            y: prev.y - (offsetY * (newScale - scale) / scale)
        }));
        setScale(newScale);
    };

    // WASD movement
    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            const moveAmount = 50 / scale; // More precise movement when zoomed in
            switch (e.key.toLowerCase()) {
                case 'w': setPosition(prev => ({ ...prev, y: prev.y + moveAmount })); break;
                case 'a': setPosition(prev => ({ ...prev, x: prev.x + moveAmount })); break;
                case 's': setPosition(prev => ({ ...prev, y: prev.y - moveAmount })); break;
                case 'd': setPosition(prev => ({ ...prev, x: prev.x - moveAmount })); break;
            }
        };

        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [scale]);

    // Canvas click handling with proper coordinate calculation
    const handleClick = (e: React.MouseEvent<HTMLCanvasElement>) => {
        if (!canvasRef.current || !wsRef.current) return;

        const canvas = canvasRef.current;
        const rect = canvas.getBoundingClientRect();

        // Calculate canvas coordinates considering zoom and pan
        const x = Math.floor(
            (e.clientX - rect.left - position.x - (rect.width / 2 - (CANVAS_WIDTH * scale) / 2)) / scale
        );
        const y = Math.floor(
            (e.clientY - rect.top - position.y - (rect.height / 2 - (CANVAS_HEIGHT * scale) / 2)) / scale
        );

        if (x >= 0 && x < CANVAS_WIDTH && y >= 0 && y < CANVAS_HEIGHT) {
            wsRef.current.send(JSON.stringify({ x, y, color: selectedColor }));
        }
    };

    return (
        <div className="flex h-max w-full bg-gray-100">
            {/* Left sidebar - Color Palette */}
            <div className="w-16 h-min bg-white shadow-md p-2 flex flex-col items-center">
                <div className="grid grid-cols-1 gap-2">
                    {PALETTE_RGB.map((color, index) => (
                        <button
                            key={index}
                            className={`
                                w-10 h-10 rounded border-2 transition-all
                                ${selectedColor === index
                                    ? 'border-blue-500 scale-110'
                                    : 'border-gray-300 hover:border-blue-300'}
                            `}
                            style={{ backgroundColor: color }}
                            onClick={() => setSelectedColor(index)}
                            title={`Color ${index}`}
                        />
                    ))}
                </div>
            </div>

            {/* Main Canvas Area */}
            <div className="flex-1 relative overflow-hidden">
                <div
                    className="w-full h-full flex items-center justify-center overflow-hidden"
                    onWheel={handleWheel}
                    style={{ touchAction: 'none' }}
                >
                    <div
                        className="relative"
                        style={{
                            transform: `translate(${position.x}px, ${position.y}px) scale(${scale})`,
                            transformOrigin: 'center center',
                            willChange: 'transform'
                        }}
                    >
                        <canvas
                            ref={canvasRef}
                            width={CANVAS_WIDTH}
                            height={CANVAS_HEIGHT}
                            className="block bg-white shadow-lg"
                            onClick={handleClick}
                            style={{
                                imageRendering: 'pixelated',
                                maxWidth: 'none'
                            }}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
}

export default HomePage;