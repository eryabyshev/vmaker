package ru.tcloud.vmaker.core.support

class CircleCollection<T>(values: Collection<T>): Collection<T> {
    data class Node<T>(val value: T, var next: Node<T>? = null)

    private var head: Node<T>
    private var tail: Node<T>
    private val elementsInCircle: Int

    init {
        if(values.isEmpty()) {
            throw IllegalArgumentException("arg0 can not be empty")
        }
        elementsInCircle = values.size
        head = Node(values.first())
        tail = head
        head.next = tail
        values.drop(1).forEach { addNode(it) }
    }

    private fun addNode(value: T) {
        val newNode = Node(value, head)
        tail.next = newNode
        tail = newNode
    }

    override fun contains(element: T): Boolean {
        return toSet().contains(element)
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return toSet().containsAll(elements)
    }

    override fun isEmpty(): Boolean = false

    override fun iterator(): Iterator<T> {
        return object : Iterator<T>{
            override fun hasNext(): Boolean = true
            override fun next(): T = next()
        }
    }

    override val size: Int
        get() = elementsInCircle

    fun next(): T {
        val next = head
        head = head.next?:throw ArrayIndexOutOfBoundsException()
        tail = tail.next?:throw ArrayIndexOutOfBoundsException()
        return next.value
    }

    fun toSet(): Set<T> {
        return (0 until size).map { next() }.toSet()
    }
}