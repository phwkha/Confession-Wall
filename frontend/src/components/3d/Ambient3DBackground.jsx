import React, { useRef, useMemo } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import * as THREE from 'three';
import { createHeartGeometry } from './heartGeometry';
import WebGLBoundary from './WebGLBoundary';
import { isMobileDevice } from '../../utils/webgl';

const HEART_COUNT = 32;
const CRYSTAL_COUNT = 20;
const DUST_COUNT = 120;

function FloatingAmethystHearts() {
  const meshRef = useRef();
  const dummy = useMemo(() => new THREE.Object3D(), []);
  const geometry = useMemo(
    () =>
      createHeartGeometry({
        depth: 0.14,
        bevelSize: 0.03,
        bevelThickness: 0.03,
        curveSegments: 12,
      }),
    []
  );

  const colors = useMemo(
    () => [
      new THREE.Color('#7c3aed'),
      new THREE.Color('#9333ea'),
      new THREE.Color('#a855f7'),
      new THREE.Color('#c084fc'),
      new THREE.Color('#d8b4fe'),
      new THREE.Color('#e879f9'),
    ],
    []
  );

  const particles = useMemo(() => {
    const arr = [];
    for (let i = 0; i < HEART_COUNT; i++) {
      arr.push({
        x: (Math.random() - 0.5) * 18,
        y: (Math.random() - 0.5) * 16,
        z: (Math.random() - 0.5) * 8 - 2,
        speedY: 0.2 + Math.random() * 0.4,
        swaySpeed: 0.7 + Math.random() * 1.1,
        swayAmplitude: 0.25 + Math.random() * 0.45,
        rotationSpeedX: (Math.random() - 0.5) * 0.7,
        rotationSpeedY: 0.35 + Math.random() * 0.7,
        rotationSpeedZ: (Math.random() - 0.5) * 0.5,
        rotX: Math.random() * Math.PI * 2,
        rotY: Math.random() * Math.PI * 2,
        rotZ: Math.random() * Math.PI * 2,
        scale: 0.14 + Math.random() * 0.22,
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

      if (p.y > 8.5) {
        p.y = -8.5;
        p.x = (Math.random() - 0.5) * 18;
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
    <instancedMesh ref={meshRef} args={[geometry, null, HEART_COUNT]}>
      <meshStandardMaterial
        roughness={0.18}
        metalness={0.75}
        transparent
        opacity={0.88}
        emissive="#a855f7"
        emissiveIntensity={0.4}
      />
    </instancedMesh>
  );
}

function FloatingCrystals() {
  const meshRef = useRef();
  const dummy = useMemo(() => new THREE.Object3D(), []);
  const geometry = useMemo(() => new THREE.OctahedronGeometry(0.22, 0), []);

  const crystalColors = useMemo(
    () => [
      new THREE.Color('#be123c'),
      new THREE.Color('#881337'),
      new THREE.Color('#e11d48'),
      new THREE.Color('#fb7185'),
    ],
    []
  );

  const crystals = useMemo(() => {
    const arr = [];
    for (let i = 0; i < CRYSTAL_COUNT; i++) {
      arr.push({
        x: (Math.random() - 0.5) * 16,
        y: (Math.random() - 0.5) * 14,
        z: (Math.random() - 0.5) * 7 - 1,
        speedY: 0.15 + Math.random() * 0.3,
        rotSpeedX: (Math.random() - 0.5) * 0.9,
        rotSpeedY: 0.5 + Math.random() * 0.8,
        rotSpeedZ: (Math.random() - 0.5) * 0.9,
        rotX: Math.random() * Math.PI * 2,
        rotY: Math.random() * Math.PI * 2,
        rotZ: Math.random() * Math.PI * 2,
        scale: 0.12 + Math.random() * 0.18,
        color: crystalColors[i % crystalColors.length],
      });
    }
    return arr;
  }, [crystalColors]);

  useFrame((state, delta) => {
    if (!meshRef.current) return;

    crystals.forEach((c, i) => {
      c.y += c.speedY * delta;
      if (c.y > 8.0) {
        c.y = -8.0;
        c.x = (Math.random() - 0.5) * 16;
      }

      c.rotX += c.rotSpeedX * delta;
      c.rotY += c.rotSpeedY * delta;
      c.rotZ += c.rotSpeedZ * delta;

      dummy.position.set(c.x, c.y, c.z);
      dummy.rotation.set(c.rotX, c.rotY, c.rotZ);
      dummy.scale.set(c.scale, c.scale, c.scale);
      dummy.updateMatrix();

      meshRef.current.setMatrixAt(i, dummy.matrix);
      meshRef.current.setColorAt(i, c.color);
    });

    meshRef.current.instanceMatrix.needsUpdate = true;
    if (meshRef.current.instanceColor) {
      meshRef.current.instanceColor.needsUpdate = true;
    }
  });

  return (
    <instancedMesh ref={meshRef} args={[geometry, null, CRYSTAL_COUNT]}>
      <meshStandardMaterial
        roughness={0.15}
        metalness={0.85}
        transparent
        opacity={0.75}
        emissive="#881337"
        emissiveIntensity={0.25}
      />
    </instancedMesh>
  );
}

function LuminousDust() {
  const pointsRef = useRef();

  const [positions] = useMemo(() => {
    const pos = new Float32Array(DUST_COUNT * 3);
    for (let i = 0; i < DUST_COUNT; i++) {
      pos[i * 3] = (Math.random() - 0.5) * 20;
      pos[i * 3 + 1] = (Math.random() - 0.5) * 18;
      pos[i * 3 + 2] = (Math.random() - 0.5) * 10 - 2;
    }
    return [pos];
  }, []);

  useFrame((state, delta) => {
    if (!pointsRef.current) return;
    pointsRef.current.rotation.y += delta * 0.03;
    pointsRef.current.rotation.x += delta * 0.015;
  });

  return (
    <points ref={pointsRef}>
      <bufferGeometry>
        <bufferAttribute
          attach="attributes-position"
          count={DUST_COUNT}
          array={positions}
          itemSize={3}
        />
      </bufferGeometry>
      <pointsMaterial
        size={0.06}
        color="#e9d5ff"
        transparent
        opacity={0.65}
        sizeAttenuation
        blending={THREE.AdditiveBlending}
      />
    </points>
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
          <fog attach="fog" args={['#020617', 5, 22]} />
          <ambientLight intensity={0.5} />
          <directionalLight position={[0, 8, 4]} color="#f3e8ff" intensity={0.6} />
          <pointLight position={[5, 5, 3]} color="#a855f7" intensity={2.6} distance={22} />
          <pointLight position={[-5, -4, 2]} color="#6366f1" intensity={2.0} distance={18} />
          <FloatingAmethystHearts />
          <FloatingCrystals />
          <LuminousDust />
        </Canvas>
      </div>
    </WebGLBoundary>
  );
}
