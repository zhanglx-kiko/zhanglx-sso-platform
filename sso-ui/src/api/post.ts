import request from '@/utils/request'
import type { PageResult, StatusPayload } from '@/types/common'
import type { PostDTO, PostQueryDTO } from '@/types/system'

export const getPostPageApi = (data: PostQueryDTO) => {
  return request.post<unknown, PageResult<PostDTO>>('/apis/v1/auth/s/posts/page', data)
}

export const getPostDetailApi = (postId: string) => {
  return request.get<unknown, PostDTO>(`/apis/v1/auth/s/posts/${postId}`)
}

export const createPostApi = (data: PostDTO) => {
  return request.post<unknown, void>('/apis/v1/auth/s/posts', data)
}

export const updatePostApi = (postId: string, data: PostDTO) => {
  return request.put<unknown, void>(`/apis/v1/auth/s/posts/${postId}`, data)
}

export const deletePostApi = (postId: string) => {
  return request.delete<unknown, void>(`/apis/v1/auth/s/posts/${postId}`)
}

export const batchDeletePostsApi = (postIds: string[]) => {
  return request.delete<unknown, void>('/apis/v1/auth/s/posts', {
    data: postIds,
  })
}

export const updatePostStatusApi = (postId: string, data: StatusPayload) => {
  return request.patch<unknown, void>(`/apis/v1/auth/s/posts/${postId}/status`, data)
}
