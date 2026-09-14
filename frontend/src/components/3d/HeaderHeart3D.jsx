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
    const baseSpeed = 1.0;
    const speed = isHovered ? baseSpeed * 3.0 : baseSpeed;
    meshRef.current.rotation.y += speed * delta;

    const targetRotX = isHovered ? state.pointer.y * 0.35 : 0;
    const targetRotZ = isHovered ? -state.pointer.x * 0.35 : 0;
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
        color="#f43f5e"
        emissive="#e11d48"
        emissiveIntensity={isHovered ? 0.45 : 0.25}
        roughness={0.25}
        metalness={0.2}
      />
    </mesh>
  );
}

export default function HeaderHeart3D() {
  const fallback = (
    <div
      className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-rose-500 to-pink-400 flex items-center justify-center shadow-md shadow-rose-500/20 transform hover:scale-105 transition-transform"
      aria-label="Heart Icon Fallback"
    >
      <Heart className="w-6 h-6 text-white fill-white" />
    </div>
  );

  return (
    <WebGLBoundary fallback={fallback}>
      <div
        className="w-12 h-12 relative flex items-center justify-center rounded-2xl bg-gradient-to-tr from-rose-500/10 via-pink-400/20 to-rose-200/30 border border-rose-200/60 shadow-md shadow-rose-500/10 overflow-hidden cursor-pointer select-none"
        title="Nhấp hoặc rê chuột vào trái tim 3D!"
      >
        <Canvas
          dpr={isMobileDevice() ? 1.0 : [1.0, 1.5]}
          camera={{ position: [0, 0, 2.8], fov: 42 }}
          gl={{ antialias: true, alpha: true, powerPreference: 'low-power' }}
          className="w-full h-full pointer-events-auto"
        >
          <ambientLight intensity={0.9} />
          <directionalLight position={[2, 3, 4]} intensity={1.3} />
          <pointLight position={[-2, -1, 1]} color="#f472b6" intensity={0.9} />
          <InteractiveHeartMesh />
        </Canvas>
      </div>
    </WebGLBoundary>
  );
}
