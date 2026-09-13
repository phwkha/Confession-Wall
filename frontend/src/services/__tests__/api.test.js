import { describe, it, expect, vi, beforeEach } from 'vitest';
import api, { getConfessions, createConfession, likeConfession } from '../api';

vi.mock('axios', () => {
  const mAxios = {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    create: vi.fn(function() { return this; }),
  };
  return {
    default: mAxios,
  };
});

describe('API Service contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('getConfessions calls GET /confessions', async () => {
    const mockData = [
      { id: 1, content: 'Test confession', author: 'Ẩn danh', likes: 2, createdAt: '2026-09-13T10:00:00' }
    ];
    api.get.mockResolvedValueOnce({ data: mockData });

    const result = await getConfessions();
    expect(api.get).toHaveBeenCalledWith('/confessions');
    expect(result).toEqual(mockData);
  });

  it('createConfession calls POST /confessions with trimmed payload', async () => {
    const mockPayload = { content: 'Secret love', author: '  John Doe  ' };
    const mockCreated = { id: 2, content: 'Secret love', author: 'John Doe', likes: 0, createdAt: '2026-09-13T10:05:00' };
    api.post.mockResolvedValueOnce({ data: mockCreated });

    const result = await createConfession(mockPayload);
    expect(api.post).toHaveBeenCalledWith('/confessions', {
      content: 'Secret love',
      author: 'John Doe',
    });
    expect(result).toEqual(mockCreated);
  });

  it('createConfession defaults empty author to undefined for backend defaulting', async () => {
    const mockPayload = { content: 'Anonymous secret', author: '   ' };
    const mockCreated = { id: 3, content: 'Anonymous secret', author: 'Ẩn danh', likes: 0 };
    api.post.mockResolvedValueOnce({ data: mockCreated });

    await createConfession(mockPayload);
    expect(api.post).toHaveBeenCalledWith('/confessions', {
      content: 'Anonymous secret',
      author: undefined,
    });
  });

  it('likeConfession calls PUT /confessions/{id}/like', async () => {
    const mockUpdated = { id: 1, content: 'Test', author: 'Ẩn danh', likes: 3 };
    api.put.mockResolvedValueOnce({ data: mockUpdated });

    const result = await likeConfession(1);
    expect(api.put).toHaveBeenCalledWith('/confessions/1/like');
    expect(result).toEqual(mockUpdated);
  });
});
