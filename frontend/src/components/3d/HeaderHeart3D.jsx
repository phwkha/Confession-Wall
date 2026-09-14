import React, { useRef, useState, useMemo } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import * as THREE from 'three';
import { Heart } from 'lucide-react';
import { createHeartGeometry } from './heartGeometry';
import WebGLBoundary from './WebGLBoundary';
import { isMobileDevice } from '../../utils/webgl';

function InteractiveHeartMesh() {
  const meshRef = useRef();
  const [isHovered, setIsHovered] = useState(false);
  const [clickPulse, setClickPulse] = useState(0);

  const geometry = useMemo(() => createHeartGeometry(), []);

  useFrame((state, delta) => {
    if (!meshRef.current) return;
    const baseSpeed = 0.9;
    const speed = isHovered ? baseSpeed * 2.8 : baseSpeed;
    meshRef.current.rotation.y += speed * delta;

    const targetRotX = isHovered ? state.pointer.y * 0.4 : 0;
    const targetRotZ = isHovered ? -state.pointer.x * 0.4 : 0;
    meshRef.current.rotation.x = THREE.MathUtils.lerp(meshRef.current.rotation.x, targetRotX, 0.1);
    meshRef.current.rotation.z = THREE.MathUtils.lerp(meshRef.current.rotation.z, targetRotZ, 0.1);

    const hoverScale = isHovered ? 1.18 : 1.0;
    const targetScale = hoverScale + clickPulse;
    meshRef.current.scale.lerp(new THREE.Vector3(targetScale, targetScale, targetScale), 0.15);

    if (clickPulse > 0.005) {
      setClickPulse((prev) => prev * 0.85);
    } else if (clickPulse !== 0) {
      setClickPulse(0);
    }
  });

  const handleClick = (e) => {
    e.stopPropagation();
    setClickPulse(0.35);
  };

  return (
    <mesh
      ref={meshRef}
      geometry={geometry}
      onPointerOver={(e) => {
        e.stopPropagation();
        setIsHovered(true);
      }}
      onPointerOut={(e) => {
        e.stopPropagation();
        setIsHovered(false);
      }}
      onClick={handleClick}
    >
      <meshStandardMaterial
        color="#881337"
        emissive="#be123c"
        emissiveIntensity={isHovered ? 0.8 : 0.35}
        roughness={0.18}
        metalness={0.85}
      />
    </mesh>
  );
}

export default function HeaderHeart3D() {
  const fallback = (
    <div
      className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-slate-900 to-rose-950 border border-rose-900/50 flex items-center justify-center shadow-md shadow-rose-950/50 transform hover:scale-105 transition-transform"
      aria-label="Heart Icon Fallback"
    >
      <Heart className="w-6 h-6 text-rose-500 fill-rose-500/80" />
    </div>
  );

  return (
    <WebGLBoundary fallback={fallback}>
      <div
        className="w-12 h-12 relative flex items-center justify-center rounded-2xl bg-gradient-to-tr from-slate-900/90 via-rose-950/40 to-slate-900/90 border border-rose-900/50 shadow-lg shadow-rose-950/40 hover:border-rose-500/70 hover:shadow-rose-900/60 overflow-hidden cursor-pointer select-none transition-all duration-300"
        title="Nhấp hoặc rê chuột vào trái tim 3D!"
      >
        <Canvas
          dpr={isMobileDevice() ? 1.0 : [1.0, 1.5]}
          camera={{ position: [0, 0, 2.8], fov: 42 }}
          gl={{ antialias: true, alpha: true, powerPreference: 'low-power' }}
          className="w-full h-full pointer-events-auto"
        >
          <ambientLight intensity={0.6} />
          <directionalLight position={[2, 3, 4]} intensity={1.5} color="#ffe4e6" />
          <pointLight position={[-2, -1, 1]} color="#e11d48" intensity={2.2} />
          <pointLight position={[2, -2, -1]} color="#7c3aed" intensity={1.2} />
          <InteractiveHeartMesh />
        </Canvas>
      </div>
    </WebGLBoundary>
  );
}
