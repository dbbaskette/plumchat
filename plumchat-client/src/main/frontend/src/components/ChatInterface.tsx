import React, { useState, useRef, useEffect } from 'react'
import { Send, Loader2 } from 'lucide-react'
import MessageBubble from './MessageBubble'
import SuggestionChips from './SuggestionChips'
import type { ChatMessage } from '../types/chat'

interface ChatInterfaceProps {
  messages: ChatMessage[]
  onSendMessage: (message: string) => Promise<void>
}

export default function ChatInterface({ messages, onSendMessage }: ChatInterfaceProps) {
  const [input, setInput] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [suggestions] = useState([
    "Show me all database schemas",
    "What tables are in the public schema?",
    "Describe the customers table",
    "Help me explore the database structure"
  ])
  
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLTextAreaElement>(null)

  const scrollToBottom = () => {
    // Use setTimeout to ensure layout is complete before scrolling
    setTimeout(() => {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
    }, 100)
  }

  useEffect(() => {
    // Only auto-scroll to bottom if there are multiple messages (user has started chatting)
    // For the initial welcome message, keep it at the top
    if (messages.length > 1) {
      scrollToBottom()
    }
  }, [messages])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!input.trim() || isLoading) return

    const messageContent = input.trim()
    setInput('')
    setIsLoading(true)

    try {
      await onSendMessage(messageContent)
    } catch (error) {
      console.error('Failed to send message:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const handleSuggestionClick = (suggestion: string) => {
    setInput(suggestion)
    inputRef.current?.focus()
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSubmit(e)
    }
  }

  return (
    <div className="flex flex-col h-full chat-container">
      {/* Messages area */}
      <div className="flex-1 overflow-y-auto p-6 space-y-6">
        {messages.map((message) => (
          <MessageBubble key={message.id} message={message} />
        ))}
        
        {isLoading && (
          <div className="flex justify-center py-6">
            <div className="flex items-center space-x-3 text-gray-300 bg-gray-800/50 px-6 py-3 rounded-2xl backdrop-blur-sm">
              <Loader2 className="w-5 h-5 animate-spin text-blue-400" />
              <span className="text-sm font-medium">PlumChat is thinking...</span>
            </div>
          </div>
        )}
        
        <div ref={messagesEndRef} />
      </div>

      {/* Suggestions */}
      {messages.length <= 1 && (
        <div className="px-4 pb-2">
          <SuggestionChips 
            suggestions={suggestions}
            onSuggestionClick={handleSuggestionClick}
          />
        </div>
      )}

      {/* Input area */}
      <div className="border-t border-gray-700/50 p-6 header-glass">
        <form onSubmit={handleSubmit} className="flex space-x-4">
          <div className="flex-1 relative">
            <textarea
              ref={inputRef}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Ask about your Greenplum database..."
              className="chat-input w-full px-6 py-4 rounded-2xl resize-none transition-all duration-200"
              rows={1}
              style={{
                minHeight: '56px',
                maxHeight: '120px',
                resize: 'none',
                overflow: 'auto'
              }}
            />
          </div>
          
          <button
            type="submit"
            disabled={!input.trim() || isLoading}
            className="button-primary disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-2 min-w-[120px]"
          >
            {isLoading ? (
              <Loader2 className="w-5 h-5 animate-spin" />
            ) : (
              <Send className="w-5 h-5" />
            )}
            <span className="hidden sm:inline">Send</span>
          </button>
        </form>
        
        <div className="mt-3 text-xs text-gray-400 text-center">
          Press Enter to send, Shift+Enter for new line
        </div>
      </div>
    </div>
  )
}
