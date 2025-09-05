import React, { useMemo, useState } from 'react'
import {
  useReactTable,
  getCoreRowModel,
  getSortedRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  flexRender,
  type SortingState,
  type ColumnFiltersState,
} from '@tanstack/react-table'
import { useVirtualizer } from '@tanstack/react-virtual'
import { 
  ChevronUp, 
  ChevronDown, 
  Search, 
  Download, 
  Key,
  Database,
  Type,
  ChevronLeft,
  ChevronRight
} from 'lucide-react'
import type { TableData } from '../types/chat'

interface DataTableProps {
  table: TableData
}

export default function DataTable({ table }: DataTableProps) {
  const [sorting, setSorting] = useState<SortingState>([])
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([])
  const [globalFilter, setGlobalFilter] = useState('')
  const [pagination, setPagination] = useState({
    pageIndex: 0,
    pageSize: 10
  })

  // Create columns from table data
  const columns = useMemo(() => {
    return table.columns.map((col) => ({
      accessorKey: col.name,
      header: ({ column }: any) => (
        <div className="flex items-center space-x-2">
          <button
            className="flex items-center space-x-1 hover:bg-gray-100 px-2 py-1 rounded"
            onClick={() => column.toggleSorting(column.getIsSorted() === 'asc')}
          >
            <span className="font-medium">{col.name}</span>
            {column.getIsSorted() === 'asc' && <ChevronUp className="w-3 h-3" />}
            {column.getIsSorted() === 'desc' && <ChevronDown className="w-3 h-3" />}
          </button>
          
          <div className="flex items-center space-x-1 text-xs text-gray-500">
            {col.primaryKey && (
              <div className="flex items-center space-x-1">
                <Key className="w-3 h-3 text-yellow-600" />
                <span>PK</span>
              </div>
            )}
            <div className="flex items-center space-x-1">
              <Type className="w-3 h-3" />
              <span>{col.type}</span>
            </div>
            {!col.nullable && (
              <span className="text-red-600 font-bold">*</span>
            )}
          </div>
        </div>
      ),
      cell: ({ getValue }: any) => {
        const value = getValue()
        
        if (value === null || value === undefined) {
          return <span className="text-gray-400 italic">NULL</span>
        }
        
        if (typeof value === 'string' && value.length > 100) {
          return (
            <div className="max-w-xs">
              <div className="truncate" title={value}>
                {value}
              </div>
            </div>
          )
        }
        
        return <span>{String(value)}</span>
      },
    }))
  }, [table.columns])

  // Prepare data - handle case where rows might not be provided
  const data = useMemo(() => {
    if (!table.rows || table.rows.length === 0) {
      // Show column structure only
      return []
    }
    
    return table.rows.map((row, index) => {
      const rowObj: Record<string, any> = { _id: index }
      table.columns.forEach((col, colIndex) => {
        rowObj[col.name] = row[colIndex]
      })
      return rowObj
    })
  }, [table.rows, table.columns])

  const reactTable = useReactTable({
    data,
    columns,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    onSortingChange: setSorting,
    onColumnFiltersChange: setColumnFilters,
    onGlobalFilterChange: setGlobalFilter,
    onPaginationChange: setPagination,
    state: {
      sorting,
      columnFilters,
      globalFilter,
      pagination,
    },
  })

  const exportToCSV = () => {
    const headers = table.columns.map(col => col.name).join(',')
    const rows = data.map(row => 
      table.columns.map(col => {
        const value = row[col.name]
        return value === null || value === undefined ? '' : `"${String(value).replace(/"/g, '""')}"`
      }).join(',')
    )
    
    const csv = [headers, ...rows].join('\n')
    const blob = new Blob([csv], { type: 'text/csv' })
    const url = URL.createObjectURL(blob)
    
    const a = document.createElement('a')
    a.href = url
    a.download = `${table.schema}_${table.name}.csv`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  }

  if (data.length === 0) {
    return (
      <div className="p-6">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-2">
            <Database className="w-5 h-5 text-gray-600" />
            <h3 className="text-lg font-medium">Table Structure</h3>
          </div>
        </div>
        
        <div className="bg-gray-50 rounded-lg p-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {table.columns.map((col, index) => (
              <div key={index} className="bg-gray-800/40 p-3 rounded-lg border border-gray-700/50">
                <div className="flex items-center space-x-2 mb-2">
                  <span className="font-medium text-gray-900">{col.name}</span>
                  {col.primaryKey && <Key className="w-4 h-4 text-yellow-600" />}
                </div>
                <div className="text-sm text-gray-600">
                  <div>Type: {col.type}</div>
                  <div>{col.nullable ? 'Nullable' : 'Not Null'}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
        
        <div className="mt-4 text-sm text-gray-500 text-center">
          <p>No data rows to display. This shows the table structure only.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="p-4">
      {/* Table controls */}
      <div className="flex items-center justify-between mb-4 space-x-4">
        <div className="flex items-center space-x-2 flex-1">
          <Search className="w-4 h-4 text-gray-400" />
          <input
            value={globalFilter ?? ''}
            onChange={(e) => setGlobalFilter(e.target.value)}
            placeholder="Search all columns..."
            className="border border-gray-300 rounded px-3 py-1 text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent flex-1 max-w-sm"
          />
        </div>
        
        <div className="flex items-center space-x-2">
          <span className="text-sm text-gray-600">
            {reactTable.getFilteredRowModel().rows.length} rows
          </span>
          
          <button
            onClick={exportToCSV}
            className="button-secondary text-sm flex items-center space-x-1"
          >
            <Download className="w-4 h-4" />
            <span>Export CSV</span>
          </button>
        </div>
      </div>

      {/* Table */}
      <div className="border border-gray-700/50 rounded-lg overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full">
            <thead className="bg-gray-50">
              {reactTable.getHeaderGroups().map((headerGroup) => (
                <tr key={headerGroup.id}>
                  {headerGroup.headers.map((header) => (
                    <th
                      key={header.id}
                      className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider border-b border-gray-700/50"
                    >
                      {header.isPlaceholder
                        ? null
                        : flexRender(header.column.columnDef.header, header.getContext())}
                    </th>
                  ))}
                </tr>
              ))}
            </thead>
            <tbody className="bg-gray-800/40 divide-y divide-gray-700/50">
              {reactTable.getRowModel().rows.map((row) => (
                <tr key={row.id} className="hover:bg-gray-50">
                  {row.getVisibleCells().map((cell) => (
                    <td
                      key={cell.id}
                      className="px-4 py-3 text-sm text-gray-900 border-b border-gray-100"
                    >
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Pagination */}
      {reactTable.getPageCount() > 1 && (
        <div className="flex items-center justify-between mt-4">
          <div className="flex items-center space-x-2">
            <span className="text-sm text-gray-600">
              Page {reactTable.getState().pagination.pageIndex + 1} of{' '}
              {reactTable.getPageCount()}
            </span>
          </div>
          
          <div className="flex items-center space-x-2">
            <button
              onClick={() => reactTable.previousPage()}
              disabled={!reactTable.getCanPreviousPage()}
              className="button-secondary disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            
            <button
              onClick={() => reactTable.nextPage()}
              disabled={!reactTable.getCanNextPage()}
              className="button-secondary disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
