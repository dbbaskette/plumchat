import React, { useState, useEffect } from 'react'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import ChatInterface from './components/ChatInterface'
import Header from './components/Header'
import Sidebar from './components/Sidebar'
import StatusPanel from './components/StatusPanel'
import { chatService } from './services/chatService'
import type { ChatMessage, LlmStatus } from './types/chat'

function App() {
  const [sessionId] = useState(() => crypto.randomUUID())
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [isConnected, setIsConnected] = useState(false)
  const [llmStatus, setLlmStatus] = useState<LlmStatus | null>(null)
  const [sidebarOpen, setSidebarOpen] = useState(false)

  useEffect(() => {
    // Check initial connection status
    checkConnectionStatus()
    
    // Load initial welcome message
    const welcomeMessage: ChatMessage = {
      id: crypto.randomUUID(),
      content: `Welcome to PlumChat! 🌱

I'm your AI assistant for Greenplum database operations.

**Quick Start:**
• Explore database schemas and tables
• Get detailed table information  
• Build and execute SQL queries
• Database administration tasks

What would you like to explore today?`,
      role: 'assistant',
      timestamp: new Date().toISOString(),
      data: null
    }
    
    setMessages([welcomeMessage])
  }, [])

  const checkConnectionStatus = async () => {
    try {
      const [mcpStatus, llmStatusData] = await Promise.all([
        chatService.getConnectionStatus(),
        chatService.getLlmStatus()
      ])
      setIsConnected(Object.values(mcpStatus).some(connected => connected))
      setLlmStatus(llmStatusData)
    } catch (error) {
      console.error('Failed to check connection status:', error)
      setIsConnected(false)
      setLlmStatus(null)
    }
  }

  const handleSendMessage = async (content: string) => {
    // First, add the user's message to the chat
    const userMessage: ChatMessage = {
      id: crypto.randomUUID(),
      content: content,
      role: 'user',
      timestamp: new Date().toISOString(),
      data: null
    }
    
    setMessages(prev => [...prev, userMessage])
    
    try {
      const response = await chatService.sendMessage({
        message: content,
        sessionId
      })
      
      // Then add the assistant's response
      setMessages(prev => [...prev, response.message])
    } catch (error) {
      console.error('Failed to send message:', error)
      
      // Add error message
      const errorMessage: ChatMessage = {
        id: crypto.randomUUID(),
        content: 'Sorry, I encountered an error processing your message. Please try again.',
        role: 'assistant',
        timestamp: new Date().toISOString(),
        data: {
          type: 'error',
          error: 'Connection failed',
          tables: null,
          schemas: null
        }
      }
      
      setMessages(prev => [...prev, errorMessage])
    }
  }

  return (
    <Router>
      <div className="flex h-screen app-container">
        {/* Sidebar */}
        <Sidebar 
          isOpen={sidebarOpen} 
          onClose={() => setSidebarOpen(false)}
          sessionId={sessionId}
          messages={messages}
        />
        
        {/* Main content */}
        <div className="flex-1 flex flex-col overflow-hidden">
          {/* Header */}
          <Header 
            onMenuClick={() => setSidebarOpen(true)}
            isConnected={isConnected}
            onRefreshStatus={checkConnectionStatus}
          />
          
          {/* Main chat area */}
          <main className="flex-1 flex overflow-hidden">
            <div className="flex-1 flex flex-col">
              <Routes>
                <Route 
                  path="/" 
                  element={
                    <ChatInterface
                      messages={messages}
                      onSendMessage={handleSendMessage}
                    />
                  } 
                />
              </Routes>
            </div>
            
            {/* Status Panel */}
            <div className="hidden lg:block w-80 border-l border-gray-200">
              <StatusPanel 
                isConnected={isConnected}
                onRefreshStatus={checkConnectionStatus}
              />
            </div>
          </main>
        </div>
        
        {/* Mobile sidebar backdrop */}
        {sidebarOpen && (
          <div 
            className="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
            onClick={() => setSidebarOpen(false)}
          />
        )}
      </div>
    </Router>
  )
}

export default App
