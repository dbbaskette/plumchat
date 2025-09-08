import React, { useMemo, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
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
import { 
  ChevronUp, 
  ChevronDown, 
  Search, 
  Download, 
  Key,
  Database,
  Type,
  ChevronLeft,
  ChevronRight,
  MoreHorizontal,
  SortAsc,
  SortDesc,
  Filter
} from 'lucide-react'
import type { TableData } from '../types/chat'
import { Button } from './ui/button'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from './ui/table'
import { cn } from '../lib/utils'

interface EnhancedDataTableProps {
  table: TableData
}

export default function EnhancedDataTable({ table }: EnhancedDataTableProps) {
  const [sorting, setSorting] = useState<SortingState>([])
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([])
  const [globalFilter, setGlobalFilter] = useState('')
  const [pagination, setPagination] = useState({
    pageIndex: 0,
    pageSize: 50 // Increased default page size
  })

  // Create columns from table data with enhanced features
  const columns = useMemo(() => {
    return table.columns.map((col) => ({
      accessorKey: col.name,
      header: ({ column }: any) => (
        <motion.div 
          className="flex items-center space-x-2 min-w-0"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.3 }}
        >
          <Button
            variant="ghost"
            onClick={() => column.toggleSorting(column.getIsSorted() === 'asc')}
            className="h-8 px-2 lg:px-3 hover:bg-accent/50 transition-all duration-200"
          >
            <span className="font-medium text-foreground truncate">{col.name}</span>
            {column.getIsSorted() === 'asc' ? (
              <motion.div
                initial={{ rotate: 180 }}
                animate={{ rotate: 0 }}
                transition={{ duration: 0.2 }}
              >
                <SortAsc className="ml-2 h-4 w-4" />
              </motion.div>
            ) : column.getIsSorted() === 'desc' ? (
              <motion.div
                initial={{ rotate: 0 }}
                animate={{ rotate: 180 }}
                transition={{ duration: 0.2 }}
              >
                <SortDesc className="ml-2 h-4 w-4" />
              </motion.div>
            ) : (
              <MoreHorizontal className="ml-2 h-4 w-4 opacity-50" />
            )}
          </Button>
          
          <div className="flex items-center space-x-1 text-xs text-muted-foreground">
            {col.primaryKey && (
              <motion.div 
                className="flex items-center space-x-1"
                initial={{ scale: 0 }}
                animate={{ scale: 1 }}
                transition={{ delay: 0.1 }}
              >
                <Key className="h-3 w-3 text-yellow-500" />
                <span className="text-yellow-500 font-medium">PK</span>
              </motion.div>
            )}
            <div className="flex items-center space-x-1">
              <Type className="h-3 w-3" />
              <span className="font-mono text-xs">{col.type}</span>
            </div>
            {!col.nullable && (
              <span className="text-destructive font-bold">*</span>
            )}
          </div>
        </motion.div>
      ),
      cell: ({ getValue }: any) => {
        const value = getValue()
        
        if (value === null || value === undefined) {
          return (
            <span className="text-muted-foreground italic font-mono text-sm">
              NULL
            </span>
          )
        }
        
        if (typeof value === 'string' && value.length > 50) {
          return (
            <div className="max-w-[200px]">
              <div className="truncate text-sm" title={value}>
                {value}
              </div>
            </div>
          )
        }
        
        return (
          <span className="whitespace-nowrap text-sm font-mono">
            {String(value)}
          </span>
        )
      },
    }))
  }, [table.columns])

  // Prepare data
  const data = useMemo(() => {
    if (!table.rows || table.rows.length === 0) {
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
      <motion.div 
        className="p-6 border border-border rounded-lg bg-card"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
      >
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-2">
            <Database className="h-5 w-5 text-primary" />
            <h3 className="text-lg font-medium text-card-foreground">Table Structure</h3>
          </div>
        </div>
        
        <motion.div 
          className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.2, stagger: 0.1 }}
        >
          {table.columns.map((col, index) => (
            <motion.div 
              key={index} 
              className="border border-border p-4 rounded-lg bg-card hover:bg-accent/50 transition-colors duration-200"
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.05 }}
            >
              <div className="flex items-center space-x-2 mb-2">
                <span className="font-medium text-card-foreground">{col.name}</span>
                {col.primaryKey && <Key className="h-4 w-4 text-yellow-500" />}
              </div>
              <div className="text-sm text-muted-foreground space-y-1">
                <div className="flex items-center space-x-2">
                  <Type className="h-3 w-3" />
                  <span className="font-mono">{col.type}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <span className={col.nullable ? 'text-muted-foreground' : 'text-destructive'}>
                    {col.nullable ? 'Nullable' : 'Required'}
                  </span>
                  {!col.nullable && <span className="text-destructive">*</span>}
                </div>
              </div>
            </motion.div>
          ))}
        </motion.div>
        
        <motion.div 
          className="mt-4 text-sm text-muted-foreground text-center"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.4 }}
        >
          <p>No data rows to display. This shows the table structure only.</p>
        </motion.div>
      </motion.div>
    )
  }

  return (
    <motion.div 
      className="space-y-4"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4 }}
    >
      {/* Enhanced Controls */}
      <motion.div 
        className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between"
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <div className="flex items-center space-x-2 flex-1 min-w-0">
          <Search className="h-4 w-4 text-muted-foreground shrink-0" />
          <input
            value={globalFilter ?? ''}
            onChange={(e) => setGlobalFilter(e.target.value)}
            placeholder="Search all columns..."
            className="flex-1 px-3 py-2 text-sm bg-background border border-input rounded-md focus:outline-none focus:ring-2 focus:ring-ring focus:border-transparent transition-all duration-200"
          />
        </div>
        
        <div className="flex items-center space-x-2 shrink-0">
          <motion.div
            className="text-sm text-muted-foreground px-3 py-2 rounded-md bg-muted/50"
            key={reactTable.getFilteredRowModel().rows.length}
            initial={{ scale: 0.9, opacity: 0.5 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ duration: 0.2 }}
          >
            {reactTable.getFilteredRowModel().rows.length} rows
          </motion.div>
          
          <Button
            onClick={exportToCSV}
            variant="outline"
            size="sm"
            className="shrink-0"
          >
            <Download className="h-4 w-4 mr-2" />
            Export CSV
          </Button>
        </div>
      </motion.div>

      {/* Enhanced Table */}
      <motion.div 
        className="border border-border rounded-lg overflow-hidden bg-card"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.2 }}
      >
        <div className="overflow-x-auto max-w-full">
          <Table>
            <TableHeader>
              {reactTable.getHeaderGroups().map((headerGroup) => (
                <TableRow key={headerGroup.id}>
                  {headerGroup.headers.map((header) => (
                    <TableHead key={header.id} className="whitespace-nowrap">
                      {header.isPlaceholder
                        ? null
                        : flexRender(header.column.columnDef.header, header.getContext())}
                    </TableHead>
                  ))}
                </TableRow>
              ))}
            </TableHeader>
            <TableBody>
              <AnimatePresence mode="wait">
                {reactTable.getRowModel().rows.map((row, index) => (
                  <motion.tr
                    key={row.id}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -10 }}
                    transition={{ delay: index * 0.02, duration: 0.2 }}
                    className="hover:bg-muted/50 transition-colors duration-200"
                  >
                    {row.getVisibleCells().map((cell) => (
                      <TableCell key={cell.id}>
                        {flexRender(cell.column.columnDef.cell, cell.getContext())}
                      </TableCell>
                    ))}
                  </motion.tr>
                ))}
              </AnimatePresence>
            </TableBody>
          </Table>
        </div>
      </motion.div>

      {/* Enhanced Pagination */}
      {reactTable.getPageCount() > 1 && (
        <motion.div 
          className="flex items-center justify-between"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.3 }}
        >
          <div className="flex items-center space-x-2 text-sm text-muted-foreground">
            <span>
              Page {reactTable.getState().pagination.pageIndex + 1} of{' '}
              {reactTable.getPageCount()}
            </span>
          </div>
          
          <div className="flex items-center space-x-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => reactTable.previousPage()}
              disabled={!reactTable.getCanPreviousPage()}
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>
            
            <Button
              variant="outline"  
              size="sm"
              onClick={() => reactTable.nextPage()}
              disabled={!reactTable.getCanNextPage()}
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </motion.div>
      )}
    </motion.div>
  )
}