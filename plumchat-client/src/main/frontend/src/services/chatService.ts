import axios from 'axios'
import type { ChatRequest, ChatResponse, ConnectionStatus, ChatMessage } from '../types/chat'

const API_BASE = '/api'

class ChatService {
  private axiosInstance = axios.create({
    baseURL: API_BASE,
    headers: {
      'Content-Type': 'application/json',
    },
  })

  async sendMessage(request: ChatRequest): Promise<ChatResponse> {
    const response = await this.axiosInstance.post<ChatResponse>('/chat/message', request)
    return response.data
  }

  async getSessionHistory(sessionId: string): Promise<ChatMessage[]> {
    const response = await this.axiosInstance.get<ChatMessage[]>(`/chat/history/${sessionId}`)
    return response.data
  }

  async getConnectionStatus(): Promise<ConnectionStatus> {
    const response = await this.axiosInstance.get<ConnectionStatus>('/status/connections')
    return response.data
  }

  async getHealthStatus(): Promise<{ status: string; timestamp: string; application: string; version: string }> {
    const response = await this.axiosInstance.get('/status/health')
    return response.data
  }

  async getLlmStatus(): Promise<{ connected: boolean; healthy: boolean; model: string; hasApiKey: boolean; hasModel: boolean; hasChatClient: boolean }> {
    const response = await this.axiosInstance.get('/status/llm')
    return response.data
  }
}

export const chatService = new ChatService()
