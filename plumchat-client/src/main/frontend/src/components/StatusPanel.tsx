import React, { useState, useEffect } from 'react'
import { Circle, RefreshCw, Database, Search, Cpu, AlertCircle, Brain } from 'lucide-react'
import { chatService } from '../services/chatService'
import type { ConnectionStatus, LlmStatus, ServiceStatus } from '../types/chat'

interface StatusPanelProps {
  isConnected: boolean
  onRefreshStatus: () => void
}

export default function StatusPanel({ isConnected, onRefreshStatus }: StatusPanelProps) {
  const [connections, setConnections] = useState<ConnectionStatus>({
    schema: { connected: false, status: 'UNKNOWN', note: 'Loading...' },
    query: { connected: false, status: 'UNKNOWN', note: 'Loading...' },
    mgmt: { connected: false, status: 'UNKNOWN', note: 'Loading...' }
  })
  const [loading, setLoading] = useState(false)
  const [healthInfo, setHealthInfo] = useState<any>(null)
  const [llmStatus, setLlmStatus] = useState<LlmStatus | null>(null)

  const loadStatus = async () => {
    setLoading(true)
    try {
      const [connectionsData, healthData, llmData] = await Promise.all([
        chatService.getConnectionStatus(),
        chatService.getHealthStatus(),
        chatService.getLlmStatus()
      ])
      
      setConnections(connectionsData)
      setHealthInfo(healthData)
      setLlmStatus(llmData)
    } catch (error) {
      console.error('Failed to load status:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadStatus()
  }, [])

  const handleRefresh = () => {
    loadStatus()
    onRefreshStatus()
  }

  const services = [
    {
      name: 'Schema Server',
      key: 'schema' as keyof ConnectionStatus,
      icon: Database,
      description: 'Database schema discovery',
      port: '8080'
    },
    {
      name: 'Query Server', 
      key: 'query' as keyof ConnectionStatus,
      icon: Search,
      description: 'SQL query execution',
      port: '8081'
    },
    {
      name: 'Management Server',
      key: 'mgmt' as keyof ConnectionStatus,
      icon: Cpu,
      description: 'Database administration',
      port: '8082'
    }
  ]

  return (
    <div className="h-full bg-gray-900/50 border-l border-gray-700/50 backdrop-blur-sm">
      <div className="p-6 border-b border-gray-700/50">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold text-white">System Status</h3>
          <button
            onClick={handleRefresh}
            disabled={loading}
            className="p-2 hover:bg-gray-700/50 rounded-xl transition-all duration-200"
          >
            <RefreshCw className={`w-4 h-4 text-gray-300 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      <div className="p-6 space-y-6">
        {/* Overall Status */}
        <div className="bg-gray-800/40 rounded-xl p-4 border border-gray-700/50 backdrop-blur-sm">
          <div className="flex items-center space-x-2 mb-2">
            <Circle className={`w-4 h-4 ${isConnected ? 'text-emerald-400 fill-current' : 'text-red-400 fill-current'}`} />
            <span className="font-medium text-white">
              {isConnected ? 'Connected' : 'Disconnected'}
            </span>
          </div>
          
          {healthInfo && (
            <div className="text-sm text-gray-300 space-y-1">
              <div>Version: {healthInfo.version}</div>
              <div>Status: {healthInfo.status}</div>
            </div>
          )}
        </div>

        {/* Service Status */}
        <div>
          <h4 className="text-sm font-medium text-white mb-3">MCP Services</h4>
          <div className="space-y-3">
            {services.map((service) => {
              const serviceStatus = connections[service.key]
              const isServiceConnected = serviceStatus.connected
              const IconComponent = service.icon
              
              // Determine status color based on status type
              const getStatusColor = (status: string) => {
                switch (status) {
                  case 'ONLINE':
                    return 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                  case 'OFFLINE':
                    return 'bg-red-500/20 text-red-400 border border-red-500/30'
                  case 'NOT_IMPLEMENTED':
                    return 'bg-gray-500/20 text-gray-400 border border-gray-500/30'
                  default:
                    return 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30'
                }
              }
              
              return (
                <div key={service.key} className="border border-gray-700/50 rounded-xl p-4 bg-gray-800/30 backdrop-blur-sm">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center space-x-2">
                      <IconComponent className="w-4 h-4 text-gray-300" />
                      <span className="font-medium text-white">{service.name}</span>
                    </div>
                    <Circle className={`w-3 h-3 ${isServiceConnected ? 'text-emerald-400 fill-current' : 'text-red-400 fill-current'}`} />
                  </div>
                  
                  <div className="text-sm text-gray-300 space-y-1">
                    <div>{service.description}</div>
                    <div className="flex items-center space-x-2">
                      <span>Port: {service.port}</span>
                      <span className={`px-2 py-1 rounded-full text-xs ${getStatusColor(serviceStatus.status)}`}>
                        {serviceStatus.status}
                      </span>
                    </div>
                    {serviceStatus.note && (
                      <div className="text-xs text-gray-400 italic">
                        {serviceStatus.note}
                      </div>
                    )}
                  </div>
                </div>
              )
            })}
          </div>
        </div>

        {/* LLM Status */}
        <div>
          <h4 className="text-sm font-medium text-white mb-3">LLM Status</h4>
          <div className="border border-gray-700/50 rounded-xl p-4 bg-gray-800/30 backdrop-blur-sm">
            <div className="flex items-center justify-between mb-2">
              <div className="flex items-center space-x-2">
                <Brain className="w-4 h-4 text-gray-300" />
                <span className="font-medium text-white">AI Model</span>
              </div>
              <Circle className={`w-3 h-3 ${llmStatus?.connected ? 'text-emerald-400 fill-current' : 'text-red-400 fill-current'}`} />
            </div>
            
            <div className="text-sm text-gray-300 space-y-1">
              <div>Model: {llmStatus?.model || 'Not configured'}</div>
              <div className="flex items-center space-x-2">
                <span className={`px-2 py-1 rounded-full text-xs ${
                  llmStatus?.connected 
                    ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' 
                    : 'bg-red-500/20 text-red-400 border border-red-500/30'
                }`}>
                  {llmStatus?.connected ? 'Connected' : 'Disconnected'}
                </span>
                {llmStatus?.healthy && (
                  <span className="px-2 py-1 rounded-full text-xs bg-blue-500/20 text-blue-400 border border-blue-500/30">
                    Healthy
                  </span>
                )}
              </div>
              {!llmStatus?.hasApiKey && (
                <div className="text-xs text-amber-400">
                  ⚠️ API key not configured
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Connection Issues */}
        {!isConnected && (
          <div className="bg-red-500/10 border border-red-500/30 rounded-xl p-4 backdrop-blur-sm">
            <div className="flex items-center space-x-2 mb-2">
              <AlertCircle className="w-4 h-4 text-red-400" />
              <span className="font-medium text-red-300">Connection Issues</span>
            </div>
            <p className="text-sm text-red-200">
              One or more services are offline. Some features may not be available.
            </p>
          </div>
        )}

      </div>
    </div>
  )
}
