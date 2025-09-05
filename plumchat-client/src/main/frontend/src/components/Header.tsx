import React from 'react'
import { Menu, RefreshCw, Circle } from 'lucide-react'

interface HeaderProps {
  onMenuClick: () => void
  isConnected: boolean
  onRefreshStatus: () => void
}

export default function Header({ onMenuClick, isConnected, onRefreshStatus }: HeaderProps) {
  return (
    <header className="header-glass px-6 py-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <button
            onClick={onMenuClick}
            className="lg:hidden p-2 hover:bg-gray-700/50 rounded-xl transition-all duration-200"
          >
            <Menu className="w-5 h-5 text-gray-300" />
          </button>
          
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl flex items-center justify-center shadow-lg">
              <span className="text-white font-bold text-xl">🌱</span>
            </div>
            <div>
              <h1 className="text-xl font-bold text-white">PlumChat</h1>
              <p className="text-sm text-gray-300">Greenplum AI Assistant</p>
            </div>
          </div>
        </div>
        
        <div className="flex items-center space-x-6">
          {/* MCP Services Status */}
          <div className="flex items-center space-x-2">
            <Circle className={`w-3 h-3 ${isConnected ? 'text-emerald-400 fill-current' : 'text-red-400 fill-current'}`} />
            <span className="text-sm text-gray-300 font-medium">
              MCP {isConnected ? 'Connected' : 'Disconnected'}
            </span>
          </div>
          
          
          <button
            onClick={onRefreshStatus}
            className="p-2 hover:bg-gray-700/50 rounded-xl transition-all duration-200"
          >
            <RefreshCw className="w-4 h-4 text-gray-300" />
          </button>
        </div>
      </div>
    </header>
  )
}
