import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter'
import { oneLight } from 'react-syntax-highlighter/dist/esm/styles/prism'
import { User, Bot, Database, AlertCircle, Table } from 'lucide-react'
import { formatDistanceToNow } from 'date-fns'
import { motion, AnimatePresence } from 'framer-motion'
import EnhancedDataTable from './EnhancedDataTable'
import SchemaViewer from './SchemaViewer'
import type { ChatMessage } from '../types/chat'

interface MessageBubbleProps {
  message: ChatMessage
}

export default function MessageBubble({ message }: MessageBubbleProps) {
  const isUser = message.role === 'user'
  const timestamp = new Date(message.timestamp)
  const hasTables = message.data?.tables && message.data.tables.length > 0
  
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

        {/* Render structured data with animations */}
        <AnimatePresence>
          {message.data && (
            <motion.div 
              className="space-y-6 mt-6"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              transition={{ duration: 0.3 }}
            >
              {/* Enhanced Tables */}
              {message.data.tables && message.data.tables.length > 0 && (
                <div className="space-y-6">
                  {message.data.tables.map((table, index) => (
                    <motion.div 
                      key={index}
                      initial={{ opacity: 0, y: 30 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ 
                        duration: 0.4, 
                        delay: index * 0.1,
                        ease: "easeOut" 
                      }}
                      className="space-y-2"
                    >
                      <motion.div 
                        className="flex items-center space-x-2 px-1"
                        initial={{ opacity: 0, x: -20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ delay: index * 0.1 + 0.2 }}
                      >
                        <Table className="w-5 h-5 text-primary" />
                        <span className="font-medium text-foreground text-lg">
                          {table.schema}.{table.name}
                        </span>
                        <span className="text-sm text-muted-foreground">
                          ({table.rows?.length || 0} rows)
                        </span>
                      </motion.div>
                      <EnhancedDataTable table={table} />
                    </motion.div>
                  ))}
                </div>
              )}

              {/* Enhanced Schemas */}
              {message.data.schemas && message.data.schemas.length > 0 && (
                <div className="space-y-4">
                  {message.data.schemas.map((schema, index) => (
                    <motion.div 
                      key={index}
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ 
                        duration: 0.3, 
                        delay: (message.data.tables?.length || 0) * 0.1 + index * 0.1
                      }}
                      className="border border-border rounded-lg overflow-hidden bg-card shadow-sm"
                    >
                      <div className="bg-muted/50 px-4 py-3 border-b border-border">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center space-x-2">
                            <Database className="w-4 h-4 text-primary" />
                            <span className="font-medium text-card-foreground">
                              Schema: {schema.name}
                            </span>
                          </div>
                          <span className="text-sm text-muted-foreground bg-secondary px-2 py-1 rounded-full">
                            {schema.tables.length} tables
                          </span>
                        </div>
                      </div>
                      <SchemaViewer schema={schema} />
                    </motion.div>
                  ))}
                </div>
              )}
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    )
  }

  return (
    <motion.div 
      className={`flex space-x-3 ${isUser ? 'justify-end' : 'justify-start'}`}
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3, ease: "easeOut" }}
    >
      {!isUser && (
        <div className="flex-shrink-0">
          <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center">
            <Bot className="w-4 h-4 text-blue-600" />
          </div>
        </div>
      )}
      
      <div className={`flex-1 ${isUser ? 'order-1' : ''}`}>
        <div className={`chat-message ${isUser ? 'user' : 'assistant'} ${
          isUser ? '' : (hasTables ? 'max-w-none' : 'max-w-3xl')
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
    </motion.div>
  )
}
