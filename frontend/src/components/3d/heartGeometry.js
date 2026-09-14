import * as THREE from 'three';

export function createHeartShape() {
  const shape = new THREE.Shape();
  shape.moveTo(0, -0.55);
  shape.bezierCurveTo(-0.25, -0.2, -0.65, 0.1, -0.65, 0.42);
  shape.bezierCurveTo(-0.65, 0.72, -0.22, 0.78, 0, 0.45);
  shape.bezierCurveTo(0.22, 0.78, 0.65, 0.72, 0.65, 0.42);
  shape.bezierCurveTo(0.65, 0.1, 0.25, -0.2, 0, -0.55);
  return shape;
}

export function createHeartGeometry(options = {}) {
  const shape = createHeartShape();
  const extrudeSettings = {
    depth: options.depth || 0.22,
    bevelEnabled: options.bevelEnabled !== false,
    bevelSegments: options.bevelSegments || 4,
    steps: options.steps || 1,
    bevelSize: options.bevelSize || 0.06,
    bevelThickness: options.bevelThickness || 0.06,
    curveSegments: options.curveSegments || 24,
  };
  const geometry = new THREE.ExtrudeGeometry(shape, extrudeSettings);
  geometry.center();
  return geometry;
}
