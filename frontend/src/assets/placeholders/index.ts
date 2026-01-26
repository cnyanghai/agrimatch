// 帖子封面占位图 - 农业主题
import cover1 from './post-cover-1.jpg'
import cover2 from './post-cover-2.jpg'
import cover3 from './post-cover-3.jpg'
import cover4 from './post-cover-4.jpg'
import cover5 from './post-cover-5.jpg'
import cover6 from './post-cover-6.jpg'
import cover7 from './post-cover-7.jpg'
import cover8 from './post-cover-8.jpg'
import cover9 from './post-cover-9.jpg'
import cover10 from './post-cover-10.jpg'

export const postCoverPlaceholders = [
  cover1,
  cover2,
  cover3,
  cover4,
  cover5,
  cover6,
  cover7,
  cover8,
  cover9,
  cover10
]

/**
 * 根据帖子ID获取占位封面图
 * @param postId 帖子ID
 * @returns 占位图URL
 */
export function getPostPlaceholderCover(postId: number): string {
  return postCoverPlaceholders[postId % postCoverPlaceholders.length]
}
