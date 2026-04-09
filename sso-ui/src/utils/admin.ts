import type { PageResult, TransferOption } from '@/types/common'

export const normalizeId = (value: unknown): string => {
  return value === undefined || value === null ? '' : String(value)
}

export const toPageResult = <T>(value?: Partial<PageResult<T>>): PageResult<T> => {
  return {
    records: Array.isArray(value?.records) ? value.records : [],
    total: Number(value?.total ?? 0),
    current: Number(value?.current ?? 1),
    size: Number(value?.size ?? 10),
  }
}

export const uniqueIds = (ids: Array<string | undefined | null>): string[] => {
  return Array.from(new Set(ids.map((item) => normalizeId(item)).filter(Boolean)))
}

export const countTreeNodes = <T extends { children?: T[] }>(nodes: T[]): number => {
  return nodes.reduce((total, node) => total + 1 + countTreeNodes(node.children || []), 0)
}

export const extractTreeIds = <T extends { id: string; children?: T[] }>(nodes: T[]): string[] => {
  return nodes.flatMap((node) => [node.id, ...extractTreeIds(node.children || [])])
}

export const mapToTransferOptions = <T>(
  rows: T[],
  getKey: (row: T) => string,
  getLabel: (row: T) => string,
  isDisabled?: (row: T) => boolean,
): TransferOption[] => {
  return rows.map((row) => ({
    key: getKey(row),
    label: getLabel(row),
    disabled: isDisabled?.(row),
  }))
}

export const filterTreeByKeyword = <T extends { children?: T[] }>(
  nodes: T[],
  keyword: string,
  matcher: (node: T, normalizedKeyword: string) => boolean,
): T[] => {
  const normalizedKeyword = keyword.trim()
  if (!normalizedKeyword) return nodes

  return nodes.reduce<T[]>((result, node) => {
    const children = filterTreeByKeyword(node.children || [], normalizedKeyword, matcher)
    if (matcher(node, normalizedKeyword) || children.length > 0) {
      result.push({
        ...node,
        children: children.length > 0 ? children : undefined,
      })
    }
    return result
  }, [])
}

export const findNodeById = <T extends { id: string; children?: T[] }>(
  nodes: T[],
  id: string,
): T | null => {
  for (const node of nodes) {
    if (node.id === id) return node
    const childMatched = findNodeById(node.children || [], id)
    if (childMatched) return childMatched
  }
  return null
}
