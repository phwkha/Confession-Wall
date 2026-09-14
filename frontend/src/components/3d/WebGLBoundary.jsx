import React, { Component } from 'react';
import { isWebGLAvailable } from '../../utils/webgl';

export default class WebGLBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = {
      hasError: false,
      isSupported: isWebGLAvailable(),
    };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    console.warn('[WebGLBoundary] 3D canvas caught error:', error, errorInfo);
  }

  render() {
    if (!this.state.isSupported || this.state.hasError) {
      return this.props.fallback || null;
    }
    return this.props.children;
  }
}
