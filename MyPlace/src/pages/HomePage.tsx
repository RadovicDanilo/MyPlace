import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../api/authStore";
import { useEffect, useRef, useCallback, useState } from "react";

const CANVAS_WIDTH = 1024;
const CANVAS_HEIGHT = 1024;
const WS_URL = "ws://localhost:8081/api/ws";

const PALETTE: number[] = [
    // ABGR format (alpha, blue, green, red)
    0xff000000, // Black 
    0xffffffff, // White
    0xff0000ff, // Red
    0xff00ff00, // Green
    0xffff0000, // Blue
    0xffffff00, // Cyan
    0xffff00ff, // Magenta
    0xff00ffff, // Yellow
    0xff8800ff, // Purple
    0xff0088ff, // Orange
    0xffff4444, // Light Red
    0xff44ff44, // Light Green
    0xffff8800, // Light Blue
    0xff4444ff, // Light Blue
    0xff888888, // Gray
    0xff222222, // Dark Gray
];

function HomePage() {
    const navigate = useNavigate();

    const username = useAuthStore((state) => state.username);
    const token = useAuthStore((state) => state.token);

    const wsRef = useRef<WebSocket | null>(null);

    const canvasRef = useRef<HTMLCanvasElement>(null);
    const pixelsRef = useRef<Uint32Array | null>(null);

    const frameRef = useRef<number>(0);
    const pendingUpdatesRef = useRef<{ x: number, y: number, color: number }[]>([]);


    // Initialize pixel buffer
    const initPixelBuffer = useCallback(() => {
        if (!canvasRef.current) return;

        const ctx = canvasRef.current.getContext('2d', { willReadFrequently: true });
        if (!ctx) return;

        // Create initial blank image
        const imageData = ctx.createImageData(CANVAS_WIDTH, CANVAS_HEIGHT);
        pixelsRef.current = new Uint32Array(imageData.data.buffer);

    }, []);

    // Batch render updates using requestAnimationFrame
    const renderUpdates = useCallback(() => {
        if (!canvasRef.current || !pixelsRef.current || pendingUpdatesRef.current.length === 0) {
            frameRef.current = requestAnimationFrame(renderUpdates);
            return;
        }

        const ctx = canvasRef.current.getContext('2d');
        if (!ctx) return;

        // Process all pending updates
        const updates = pendingUpdatesRef.current;
        pendingUpdatesRef.current = [];

        const imageData = ctx.getImageData(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        const pixels = new Uint32Array(imageData.data.buffer);

        for (const { x, y, color } of updates) {
            pixels[y * CANVAS_WIDTH + x] = PALETTE[color];
        }

        ctx.putImageData(imageData, 0, 0);
        frameRef.current = requestAnimationFrame(renderUpdates);
    }, []);

    // Process raw canvas bitfield 
    const processBinaryData = useCallback((data: ArrayBuffer) => {
        if (!canvasRef.current) return;

        const ctx = canvasRef.current.getContext('2d');
        if (!ctx || !pixelsRef.current) return;

        const packed = new Uint8Array(data);
        const imageData = ctx.createImageData(CANVAS_WIDTH, CANVAS_HEIGHT);
        const pixels = new Uint32Array(imageData.data.buffer);

        // Unpack 4-bit data into 32-bit colors
        for (let i = 0; i < packed.length; i++) {
            const byte = packed[i];
            const idx = i * 2;

            // High nibble (first pixel)
            if (idx < pixels.length) pixels[idx] = PALETTE[(byte >> 4) & 0x0F];
            // Low nibble (second pixel)
            if (idx + 1 < pixels.length) pixels[idx + 1] = PALETTE[byte & 0x0F];
        }

        ctx.putImageData(imageData, 0, 0);
        pixelsRef.current = pixels;
    }, []);

    // Handle individual pixel updates
    const handlePixelUpdate = useCallback((x: number, y: number, color: number) => {
        pendingUpdatesRef.current.push({ x, y, color });
    }, []);

    // Initialize WebSocket connection
    useEffect(() => {
        if (!username) {
            navigate("/auth");
            return;
        }

        initPixelBuffer();
        frameRef.current = requestAnimationFrame(renderUpdates);

        // Use Sec-WebSocket-Protocol to send token
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


    const [scale, setScale] = useState<number>(1);

    const handleWheel = (e: React.WheelEvent<HTMLDivElement>) => {
        e.preventDefault();
        const delta = e.deltaY;
        setScale((prev) =>
            delta < 0 ? Math.min(40, prev * 1.1) : Math.max(1, prev / 1.1)
        );
    };


    const [xPos, setXPos] = useState<number>(0);
    const [yPos, setYPos] = useState<number>(0);

    const [selectedColor, setSelectedColor] = useState<number>(6);

    const handelClick = (e: React.MouseEvent<HTMLDivElement>) => {
        e.preventDefault()

        if (!wsRef) return

        const x = e.clientX
        const y = e.clientY

        const color = Math.floor(Math.random() * 15)
        wsRef.current?.send(JSON.stringify({ x, y, color: color }))
    }


    return (
        <div
            className="canvas-container-zoom"
            style={{ transform: `scale(${scale})`, imageRendering: "pixelated", overflow: "auto", maxHeight: "80vh" }}
            onWheel={handleWheel}
        >
            <div
                className="canvas-container-move"
                style={{ translate: `${xPos}, ${yPos}`, overflow: "auto", maxHeight: "80vh" }}
                onClick={handelClick}
            >
                <canvas
                    ref={canvasRef}
                    width={CANVAS_WIDTH}
                    height={CANVAS_HEIGHT}
                    className="pixel-canvas"
                />
            </div>
        </div >
    );
}

export default HomePage;