import React from 'react'
import { Table } from 'lucide-react'
import type { SchemaData } from '../types/chat'

interface SchemaViewerProps {
  schema: SchemaData
}

export default function SchemaViewer({ schema }: SchemaViewerProps) {
  return (
    <div className="p-4">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-6 gap-2">
        {schema.tables.map((tableName, index) => (
          <div key={index} className="flex items-center space-x-2 p-2 bg-gray-50 hover:bg-gray-100 rounded-lg border border-gray-200 transition-colors">
            <Table className="w-3 h-3 text-gray-600 flex-shrink-0" />
            <span className="text-xs font-medium text-gray-700 break-words leading-tight" title={tableName}>
              {tableName}
            </span>
          </div>
        ))}
      </div>
      
      {schema.tables.length === 0 && (
        <div className="text-center py-8 text-gray-500">
          <Table className="w-8 h-8 mx-auto mb-2 text-gray-400" />
          <p className="text-sm">No tables found in this schema</p>
        </div>
      )}
    </div>
  )
}
