let webGLSupportCache = null;

export function isWebGLAvailable() {
  if (webGLSupportCache !== null) {
    return webGLSupportCache;
  }
  if (typeof window === 'undefined' || typeof document === 'undefined') {
    return false;
  }
  try {
    const canvas = document.createElement('canvas');
    const gl =
      canvas.getContext('webgl2') ||
      canvas.getContext('webgl') ||
      canvas.getContext('experimental-webgl');
    webGLSupportCache = Boolean(
      gl &&
        ((typeof WebGLRenderingContext !== 'undefined' && gl instanceof WebGLRenderingContext) ||
          (typeof WebGL2RenderingContext !== 'undefined' && gl instanceof WebGL2RenderingContext))
    );
    return webGLSupportCache;
  } catch (err) {
    webGLSupportCache = false;
    return false;
  }
}

export function isMobileDevice() {
  if (typeof window === 'undefined' || typeof navigator === 'undefined') {
    return false;
  }
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(
    navigator.userAgent || ''
  );
}
