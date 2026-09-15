import { useState, useEffect, useRef, useCallback } from 'react';
import { getRealtimeStreamUrl } from '../services/api';

export function useRealtimeFeed({ onNewConfession, onLikeUpdate, onNewComment, onReconnect } = {}) {
  const [connectionStatus, setConnectionStatus] = useState('connecting'); // 'connecting' | 'connected' | 'reconnecting' | 'disconnected'
  const [retryCount, setRetryCount] = useState(0);

  const eventSourceRef = useRef(null);
  const reconnectTimeoutRef = useRef(null);
  const hasConnectedOnceRef = useRef(false);
  const isMountedRef = useRef(true);
  const retryCountRef = useRef(0);
  const connectionStatusRef = useRef('connecting');

  useEffect(() => {
    connectionStatusRef.current = connectionStatus;
  }, [connectionStatus]);

  // Store latest callbacks in ref to prevent reconnect loops on parent re-renders
  const callbacksRef = useRef({ onNewConfession, onLikeUpdate, onNewComment, onReconnect });
  useEffect(() => {
    callbacksRef.current = { onNewConfession, onLikeUpdate, onNewComment, onReconnect };
  });

  const connect = useCallback(() => {
    if (!isMountedRef.current) return;

    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }

    const url = getRealtimeStreamUrl();
    const es = new EventSource(url);
    eventSourceRef.current = es;

    es.onopen = () => {
      if (!isMountedRef.current) return;
      setConnectionStatus('connected');
      retryCountRef.current = 0;
      setRetryCount(0);

      if (hasConnectedOnceRef.current && callbacksRef.current.onReconnect) {
        callbacksRef.current.onReconnect();
      }
      hasConnectedOnceRef.current = true;
    };

    es.addEventListener('CONNECTED', () => {
      if (!isMountedRef.current) return;
      setConnectionStatus('connected');
    });

    es.addEventListener('NEW_CONFESSION', (event) => {
      try {
        const payload = JSON.parse(event.data);
        if (callbacksRef.current.onNewConfession) {
          callbacksRef.current.onNewConfession(payload);
        }
      } catch (e) {
        console.error('Failed to parse NEW_CONFESSION event:', e);
      }
    });

    es.addEventListener('LIKE_UPDATE', (event) => {
      try {
        const payload = JSON.parse(event.data);
        if (payload && payload.id != null && payload.likes != null) {
          if (callbacksRef.current.onLikeUpdate) {
            callbacksRef.current.onLikeUpdate(payload.id, payload.likes);
          }
        }
      } catch (e) {
        console.error('Failed to parse LIKE_UPDATE event:', e);
      }
    });

    es.addEventListener('NEW_COMMENT', (event) => {
      try {
        const payload = JSON.parse(event.data);
        if (callbacksRef.current.onNewComment) {
          callbacksRef.current.onNewComment(payload);
        }
      } catch (e) {
        console.error('Failed to parse NEW_COMMENT event:', e);
      }
    });

    es.onerror = () => {
      if (!isMountedRef.current) return;
      es.close();
      eventSourceRef.current = null;
      setConnectionStatus('reconnecting');

      const currentRetry = retryCountRef.current;
      const nextRetry = currentRetry + 1;
      retryCountRef.current = nextRetry;
      setRetryCount(nextRetry);

      const baseDelay = 1000;
      const delay = Math.min(30000, baseDelay * Math.pow(1.5, currentRetry)) + Math.random() * 500;

      if (reconnectTimeoutRef.current) clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = setTimeout(connect, delay);
    };
  }, []);

  useEffect(() => {
    isMountedRef.current = true;
    connect();

    const handleOnline = () => {
      if (connectionStatusRef.current !== 'connected') {
        if (reconnectTimeoutRef.current) clearTimeout(reconnectTimeoutRef.current);
        retryCountRef.current = 0;
        setRetryCount(0);
        connect();
      }
    };

    const handleOffline = () => {
      setConnectionStatus('disconnected');
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
        eventSourceRef.current = null;
      }
    };

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      isMountedRef.current = false;
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
      if (reconnectTimeoutRef.current) clearTimeout(reconnectTimeoutRef.current);
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
        eventSourceRef.current = null;
      }
    };
  }, [connect]);

  return { connectionStatus, retryCount, reconnect: connect };
}
