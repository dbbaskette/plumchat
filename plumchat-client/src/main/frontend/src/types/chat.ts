export interface ChatMessage {
  id: string
  content: string
  role: 'user' | 'assistant' | 'system'
  timestamp: string
  data?: MessageData | null
}

export interface MessageData {
  type: string
  tables?: TableData[] | null
  schemas?: SchemaData[] | null
  error?: string | null
}

export interface TableData {
  name: string
  schema: string
  columns: ColumnData[]
  rows?: any[][] | null
}

export interface ColumnData {
  name: string
  type: string
  nullable: boolean
  primaryKey: boolean
}

export interface SchemaData {
  name: string
  tables: string[]
}

export interface ChatRequest {
  message: string
  sessionId: string
}

export interface ChatResponse {
  message: ChatMessage
  sessionId: string
  status: 'SUCCESS' | 'ERROR' | 'PROCESSING'
  suggestions: string[]
}

export interface ServiceStatus {
  connected: boolean
  status: string
  note: string
}

export interface ConnectionStatus {
  schema: ServiceStatus
  query: ServiceStatus
  mgmt: ServiceStatus
}

export interface LlmStatus {
  connected: boolean
  healthy: boolean
  model: string
  hasApiKey: boolean
  hasModel: boolean
  hasChatClient: boolean
}
