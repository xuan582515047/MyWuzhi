package com.wuzhi.server.service_tool.util

import org.springframework.ai.transformer.splitter.TextSplitter

/**
 * 文档分割器
 *
 * 将文档分割成多个小块，每个小块的长度不超过2000个字符，并保留块之间的重叠部分。
 * 分割的依据是分隔符，分隔符的优先级从高到低。
 */
class DocSplitter: TextSplitter() {
    private val chunkSize: Int = 2000 // 目标块的最大字符数
    private val chunkOverlap: Int = 400 // 块之间重叠的字符数
    private val separators: List<String> = listOf("\n\n", "\n", "。", "！", "？", "……", "\\s*[.;!?]\\s*", " ", "") // 分隔符优先级从高到低

    override fun splitText(text: String): List<String> {
        return splitTextRecursively(text, separators)
    }

    private fun splitRecursiveWithSeparator(text: String, separators: List<String>): List<String> {
        if (text.length <= chunkSize) {
            return listOf(text)
        }

        val currentSeparators = if (separators.isNotEmpty()) separators else listOf("")

        for (separator in currentSeparators) {
            val regex = if (separator.isNotEmpty()) Regex(separator) else Regex("(?s).{1,$chunkSize}")
            val splits = text.split(regex).map { it.trim() }.filter { it.isNotEmpty() }

            if (splits.size > 1) {
                var chunks = mutableListOf<String>()
                var currentChunk = StringBuilder()

                for (split in splits) {
                    if (currentChunk.length + separator.length + split.length > chunkSize) {
                        if (currentChunk.isNotEmpty()) {
                            chunks.add(currentChunk.toString())
                            val overlapStart = (currentChunk.length - chunkOverlap).coerceAtLeast(0)
                            currentChunk = StringBuilder(currentChunk.substring(overlapStart))
                        }
                        if (currentChunk.isEmpty() && split.length > chunkSize) {
                            val subChunks = splitTextRecursively(split, separators.drop(1))
                            chunks.addAll(subChunks)
                            continue
                        }
                    }
                    if (currentChunk.isNotEmpty()) {
                        currentChunk.append(separator)
                    }
                    currentChunk.append(split)
                }
                if (currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk.toString())
                }
                return mergeChunksWithOverlap(chunks)
            }
        }
        return listOf(text)
    }

    private fun splitTextRecursively(text: String, separators: List<String>): List<String> {
        if (text.length <= chunkSize) {
            return listOf(text)
        }
        if (separators.isEmpty()) {
            return listOf(text)
        }
        val chunks = splitRecursiveWithSeparator(text, separators)
        val result = mutableListOf<String>()
        for (chunk in chunks) {
            if (chunk.length <= chunkSize) {
                result.add(chunk)
            } else {
                result.addAll(splitTextRecursively(chunk, separators.drop(1)))
            }
        }
        return result
    }

    private fun mergeChunksWithOverlap(chunks: MutableList<String>): List<String> {
        val merged = mutableListOf<String>()
        var i = 0
        while (i < chunks.size) {
            var currentChunk = chunks[i]
            if (i < chunks.size - 1 && (currentChunk.length + chunks[i + 1].length) <= chunkSize + chunkOverlap) {
                currentChunk += chunks[i + 1]
                i++
            }
            merged.add(currentChunk)
            i++
        }
        return merged
    }
}