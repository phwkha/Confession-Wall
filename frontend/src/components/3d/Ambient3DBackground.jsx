import React, { useRef, useMemo } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import * as THREE from 'three';
import { createHeartGeometry } from './heartGeometry';
import WebGLBoundary from './WebGLBoundary';
import { isMobileDevice } from '../../utils/webgl';

const COUNT = 28;

function FloatingMiniHearts() {
  const meshRef = useRef();
  const dummy = useMemo(() => new THREE.Object3D(), []);
  const geometry = useMemo(
    () =>
      createHeartGeometry({
        depth: 0.12,
        bevelSize: 0.03,
        bevelThickness: 0.03,
        curveSegments: 12,
      }),
    []
  );

  const colors = useMemo(() => [
    new THREE.Color('#fb7185'),
    new THREE.Color('#f472b6'),
    new THREE.Color('#fda4af'),
    new THREE.Color('#fbcfe8'),
    new THREE.Color('#f43f5e'),
  ], []);

  const particles = useMemo(() => {
    const arr = [];
    for (let i = 0; i < COUNT; i++) {
      arr.push({
        x: (Math.random() - 0.5) * 16,
        y: (Math.random() - 0.5) * 14,
        z: (Math.random() - 0.5) * 6 - 2,
        speedY: 0.25 + Math.random() * 0.45,
        swaySpeed: 0.8 + Math.random() * 1.2,
        swayAmplitude: 0.3 + Math.random() * 0.5,
        rotationSpeedX: (Math.random() - 0.5) * 0.8,
        rotationSpeedY: 0.4 + Math.random() * 0.8,
        rotationSpeedZ: (Math.random() - 0.5) * 0.6,
        rotX: Math.random() * Math.PI * 2,
        rotY: Math.random() * Math.PI * 2,
        rotZ: Math.random() * Math.PI * 2,
        scale: 0.15 + Math.random() * 0.22,
        color: colors[i % colors.length],
      });
    }
    return arr;
  }, [colors]);

  useFrame((state, delta) => {
    if (!meshRef.current) return;
    const time = state.clock.getElapsedTime();

    particles.forEach((p, i) => {
      p.y += p.speedY * delta;
      const currentX = p.x + Math.sin(time * p.swaySpeed + i) * p.swayAmplitude;

      if (p.y > 7.5) {
        p.y = -7.5;
        p.x = (Math.random() - 0.5) * 16;
      }

      p.rotX += p.rotationSpeedX * delta;
      p.rotY += p.rotationSpeedY * delta;
      p.rotZ += p.rotationSpeedZ * delta;

      dummy.position.set(currentX, p.y, p.z);
      dummy.rotation.set(p.rotX, p.rotY, p.rotZ);
      dummy.scale.set(p.scale, p.scale, p.scale);
      dummy.updateMatrix();

      meshRef.current.setMatrixAt(i, dummy.matrix);
      meshRef.current.setColorAt(i, p.color);
    });

    meshRef.current.instanceMatrix.needsUpdate = true;
    if (meshRef.current.instanceColor) {
      meshRef.current.instanceColor.needsUpdate = true;
    }
  });

  return (
    <instancedMesh ref={meshRef} args={[geometry, null, COUNT]}>
      <meshStandardMaterial roughness={0.3} metalness={0.1} transparent opacity={0.65} />
    </instancedMesh>
  );
}

export default function Ambient3DBackground() {
  return (
    <WebGLBoundary fallback={null}>
      <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden" aria-hidden="true">
        <Canvas
          dpr={isMobileDevice() ? 1.0 : 1.25}
          camera={{ position: [0, 0, 8], fov: 50 }}
          gl={{ antialias: false, alpha: true, powerPreference: 'low-power' }}
          className="w-full h-full pointer-events-none"
        >
          <ambientLight intensity={0.9} />
          <directionalLight position={[4, 6, 5]} intensity={0.8} />
          <FloatingMiniHearts />
        </Canvas>
      </div>
    </WebGLBoundary>
  );
}
