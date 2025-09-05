import React from 'react'
import { X, MessageSquare, Clock } from 'lucide-react'
import { formatDistanceToNow } from 'date-fns'
import type { ChatMessage } from '../types/chat'

interface SidebarProps {
  isOpen: boolean
  onClose: () => void
  sessionId: string
  messages: ChatMessage[]
}

export default function Sidebar({ isOpen, onClose, sessionId, messages }: SidebarProps) {
  const userMessages = messages.filter(m => m.role === 'user')
  
  return (
    <>
      <div className={`fixed inset-y-0 left-0 z-50 w-80 bg-white border-r border-gray-200 transform transition-transform duration-300 ease-in-out lg:relative lg:translate-x-0 ${
        isOpen ? 'translate-x-0' : '-translate-x-full'
      }`}>
        <div className="flex flex-col h-full">
          {/* Header */}
          <div className="flex items-center justify-between p-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">Chat History</h2>
            <button
              onClick={onClose}
              className="lg:hidden p-2 hover:bg-gray-100 rounded-lg"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
          
          {/* Session info */}
          <div className="p-4 bg-gray-50 border-b border-gray-200">
            <div className="flex items-center space-x-2 text-sm text-gray-600">
              <MessageSquare className="w-4 h-4" />
              <span>Session: {sessionId.slice(0, 8)}...</span>
            </div>
          </div>
          
          {/* Messages list */}
          <div className="flex-1 overflow-y-auto p-4">
            {userMessages.length === 0 ? (
              <div className="text-center text-gray-500 mt-8">
                <MessageSquare className="w-8 h-8 mx-auto mb-2 text-gray-400" />
                <p>No messages yet</p>
                <p className="text-sm">Start a conversation to see your chat history</p>
              </div>
            ) : (
              <div className="space-y-3">
                {userMessages.map((message) => (
                  <div key={message.id} className="p-3 bg-gray-50 rounded-lg">
                    <p className="text-sm text-gray-900 line-clamp-2">
                      {message.content}
                    </p>
                    <div className="flex items-center space-x-1 mt-2 text-xs text-gray-500">
                      <Clock className="w-3 h-3" />
                      <span>
                        {formatDistanceToNow(new Date(message.timestamp), { addSuffix: true })}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  )
}
