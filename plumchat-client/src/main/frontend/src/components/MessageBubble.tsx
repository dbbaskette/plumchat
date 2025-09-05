import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter'
import { oneLight } from 'react-syntax-highlighter/dist/esm/styles/prism'
import { User, Bot, Database, AlertCircle, Table } from 'lucide-react'
import { formatDistanceToNow } from 'date-fns'
import DataTable from './DataTable'
import SchemaViewer from './SchemaViewer'
import type { ChatMessage } from '../types/chat'

interface MessageBubbleProps {
  message: ChatMessage
}

export default function MessageBubble({ message }: MessageBubbleProps) {
  const isUser = message.role === 'user'
  const timestamp = new Date(message.timestamp)
  
  const renderMessageContent = () => {
    if (message.data?.type === 'error') {
      return (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-4">
          <div className="flex items-center space-x-2 text-red-700 mb-2">
            <AlertCircle className="w-4 h-4" />
            <span className="font-medium">Error</span>
          </div>
          <p className="text-red-600">{message.data.error}</p>
        </div>
      )
    }

    return (
      <div className="space-y-4">
        {/* Main message content */}
        <div className="prose prose-sm max-w-none">
          <ReactMarkdown
            remarkPlugins={[remarkGfm]}
            components={{
              code({ className, children }) {
                const match = /language-(\w+)/.exec(className || '')
                return match ? (
                  <SyntaxHighlighter
                    style={oneLight as any}
                    language={match[1]}
                    PreTag="div"
                  >
                    {String(children).replace(/\n$/, '')}
                  </SyntaxHighlighter>
                ) : (
                  <code className={className}>
                    {children}
                  </code>
                )
              }
            }}
          >
            {message.content}
          </ReactMarkdown>
        </div>

        {/* Render structured data */}
        {message.data && (
          <div className="space-y-4">
            {/* Tables */}
            {message.data.tables && message.data.tables.length > 0 && (
              <div className="space-y-4">
                {message.data.tables.map((table, index) => (
                  <div key={index} className="border border-gray-700/50 rounded-lg overflow-hidden">
                    <div className="bg-gray-800/40 px-4 py-3 border-b border-gray-700/50">
                      <div className="flex items-center space-x-2">
                        <Table className="w-4 h-4 text-gray-600" />
                        <span className="font-medium text-gray-900">
                          {table.schema}.{table.name}
                        </span>
                      </div>
                    </div>
                    <DataTable table={table} />
                  </div>
                ))}
              </div>
            )}

            {/* Schemas */}
            {message.data.schemas && message.data.schemas.length > 0 && (
              <div className="space-y-4">
                {message.data.schemas.map((schema, index) => (
                  <div key={index} className="border border-gray-200 rounded-lg overflow-hidden bg-gray-50 shadow-sm">
                    <div className="bg-gray-100 px-4 py-3 border-b border-gray-200">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-2">
                          <Database className="w-4 h-4 text-gray-600" />
                          <span className="font-medium text-gray-800">
                            Schema: {schema.name}
                          </span>
                        </div>
                        <span className="text-sm text-gray-600 bg-gray-200 px-2 py-1 rounded-full">
                          {schema.tables.length} tables
                        </span>
                      </div>
                    </div>
                    <SchemaViewer schema={schema} />
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    )
  }

  return (
    <div className={`flex space-x-3 ${isUser ? 'justify-end' : 'justify-start'}`}>
      {!isUser && (
        <div className="flex-shrink-0">
          <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center">
            <Bot className="w-4 h-4 text-blue-600" />
          </div>
        </div>
      )}
      
      <div className={`flex-1 ${isUser ? 'order-1' : ''}`}>
        <div className={`chat-message ${isUser ? 'user' : 'assistant'} ${
          isUser ? '' : 'max-w-3xl'
        }`}>
          {renderMessageContent()}
        </div>
        
        <div className={`mt-1 text-xs text-gray-500 ${isUser ? 'text-right' : 'text-left'}`}>
          {formatDistanceToNow(timestamp, { addSuffix: true })}
        </div>
      </div>
      
      {isUser && (
        <div className="flex-shrink-0">
          <div className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center">
            <User className="w-4 h-4 text-gray-600" />
          </div>
        </div>
      )}
    </div>
  )
}
