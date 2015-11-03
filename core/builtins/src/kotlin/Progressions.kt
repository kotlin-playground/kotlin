/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Auto-generated file. DO NOT EDIT!

package kotlin

@Deprecated("Use IntProgression instead.", ReplaceWith("IntProgression"), level = DeprecationLevel.WARNING)
/**
 * A progression of values of type `Byte`.
 */
public open class ByteProgression private constructor(
        val first: Byte,
        val last: Byte,
        override val increment: Int,
        _dummy: Boolean
) : Progression<Byte> /*, Iterable<Byte> */ {
    init {
        if (increment == 0) throw IllegalArgumentException("Increment must be non-zero")
    }

    public constructor(start: Byte, endInclusive: Byte, increment: Int) :
        this(start, kotlin.internal.getProgressionFinalElement(start.toInt(), endInclusive.toInt(), increment).toByte(), increment, false)

    @Deprecated("Use first instead.", ReplaceWith("first"))
    public override val start: Byte get() = first

    /**
     * The end value of the progression (inclusive).
     */
    @Deprecated("Use last instead.", ReplaceWith("last"))
    public override val end: Byte get() = last

    override fun iterator(): ByteIterator = ByteProgressionIterator(first, last, increment)

    /** Checks if the progression is empty. */
    public open fun isEmpty(): Boolean = if (increment > 0) first > last else first < last

    override fun equals(other: Any?): Boolean =
        other is ByteProgression && (isEmpty() && other.isEmpty() ||
        first == other.first && last == other.last && increment == other.increment)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (31 * first.toInt() + last.toInt()) + increment)

    override fun toString(): String = if (increment > 0) "$first..$last step $increment" else "$first downTo $last step ${-increment}"
}

/**
 * A progression of values of type `Char`.
 */
public open class CharProgression private constructor(
        val first: Char,
        val last: Char,
        override val increment: Int,
        _dummy: Boolean
) : Progression<Char> /*, Iterable<Char> */ {
    init {
        if (increment == 0) throw IllegalArgumentException("Increment must be non-zero")
    }

    public constructor(start: Char, endInclusive: Char, increment: Int) :
        this(start, kotlin.internal.getProgressionFinalElement(start.toInt(), endInclusive.toInt(), increment).toChar(), increment, false)

    @Deprecated("Use first instead.", ReplaceWith("first"))
    public override val start: Char get() = first

    /**
     * The end value of the progression (inclusive).
     */
    @Deprecated("Use last instead.", ReplaceWith("last"))
    public override val end: Char get() = last

    override fun iterator(): CharIterator = CharProgressionIterator(first, last, increment)

    /** Checks if the progression is empty. */
    public open fun isEmpty(): Boolean = if (increment > 0) first > last else first < last

    override fun equals(other: Any?): Boolean =
        other is CharProgression && (isEmpty() && other.isEmpty() ||
        first == other.first && last == other.last && increment == other.increment)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (31 * first.toInt() + last.toInt()) + increment)

    override fun toString(): String = if (increment > 0) "$first..$last step $increment" else "$first downTo $last step ${-increment}"
}

@Deprecated("Use IntProgression instead.", ReplaceWith("IntProgression"), level = DeprecationLevel.WARNING)
/**
 * A progression of values of type `Short`.
 */
public open class ShortProgression private constructor(
        val first: Short,
        val last: Short,
        override val increment: Int,
        _dummy: Boolean
) : Progression<Short> /*, Iterable<Short> */ {
    init {
        if (increment == 0) throw IllegalArgumentException("Increment must be non-zero")
    }

    public constructor(start: Short, endInclusive: Short, increment: Int) :
        this(start, kotlin.internal.getProgressionFinalElement(start.toInt(), endInclusive.toInt(), increment).toShort(), increment, false)

    @Deprecated("Use first instead.", ReplaceWith("first"))
    public override val start: Short get() = first

    /**
     * The end value of the progression (inclusive).
     */
    @Deprecated("Use last instead.", ReplaceWith("last"))
    public override val end: Short get() = last

    override fun iterator(): ShortIterator = ShortProgressionIterator(first, last, increment)

    /** Checks if the progression is empty. */
    public open fun isEmpty(): Boolean = if (increment > 0) first > last else first < last

    override fun equals(other: Any?): Boolean =
        other is ShortProgression && (isEmpty() && other.isEmpty() ||
        first == other.first && last == other.last && increment == other.increment)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (31 * first.toInt() + last.toInt()) + increment)

    override fun toString(): String = if (increment > 0) "$first..$last step $increment" else "$first downTo $last step ${-increment}"
}

/**
 * A progression of values of type `Int`.
 */
public open class IntProgression private constructor(
        val first: Int,
        val last: Int,
        override val increment: Int,
        _dummy: Boolean
) : Progression<Int> /*, Iterable<Int> */ {
    init {
        if (increment == 0) throw IllegalArgumentException("Increment must be non-zero")
    }

    public constructor(start: Int, endInclusive: Int, increment: Int) :
        this(start, kotlin.internal.getProgressionFinalElement(start.toInt(), endInclusive.toInt(), increment).toInt(), increment, false)

    @Deprecated("Use first instead.", ReplaceWith("first"))
    public override val start: Int get() = first

    /**
     * The end value of the progression (inclusive).
     */
    @Deprecated("Use last instead.", ReplaceWith("last"))
    public override val end: Int get() = last

    override fun iterator(): IntIterator = IntProgressionIterator(first, last, increment)

    /** Checks if the progression is empty. */
    public open fun isEmpty(): Boolean = if (increment > 0) first > last else first < last

    override fun equals(other: Any?): Boolean =
        other is IntProgression && (isEmpty() && other.isEmpty() ||
        first == other.first && last == other.last && increment == other.increment)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (31 * first + last) + increment)

    override fun toString(): String = if (increment > 0) "$first..$last step $increment" else "$first downTo $last step ${-increment}"
}

/**
 * A progression of values of type `Long`.
 */
public open class LongProgression private constructor(
        val first: Long,
        val last: Long,
        override val increment: Long,
        _dummy: Boolean
) : Progression<Long> /*, Iterable<Long> */ {
    init {
        if (increment == 0L) throw IllegalArgumentException("Increment must be non-zero")
    }

    public constructor(start: Long, endInclusive: Long, increment: Long) :
        this(start, kotlin.internal.getProgressionFinalElement(start.toLong(), endInclusive.toLong(), increment).toLong(), increment, false)

    @Deprecated("Use first instead.", ReplaceWith("first"))
    public override val start: Long get() = first

    /**
     * The end value of the progression (inclusive).
     */
    @Deprecated("Use last instead.", ReplaceWith("last"))
    public override val end: Long get() = last

    override fun iterator(): LongIterator = LongProgressionIterator(first, last, increment)

    /** Checks if the progression is empty. */
    public open fun isEmpty(): Boolean = if (increment > 0) first > last else first < last

    override fun equals(other: Any?): Boolean =
        other is LongProgression && (isEmpty() && other.isEmpty() ||
        first == other.first && last == other.last && increment == other.increment)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (31 * (first xor (first ushr 32)) + (last xor (last ushr 32))) + (increment xor (increment ushr 32))).toInt()

    override fun toString(): String = if (increment > 0) "$first..$last step $increment" else "$first downTo $last step ${-increment}"
}

/**
 * A progression of values of type `Float`.
 */
@Deprecated("This progression implementation has unclear semantics and will be removed soon.", level = DeprecationLevel.WARNING)
public open class FloatProgression(
        override val start: Float,
                 val endInclusive: Float,
        override val increment: Float
) : Progression<Float> /*, Iterable<Float> */ {
    init {
        if (java.lang.Float.isNaN(increment)) throw IllegalArgumentException("Increment must be not NaN")
        if (increment == 0.0f) throw IllegalArgumentException("Increment must be non-zero")
    }

    /**
     * The end value of the progression (inclusive).
     */
    @Deprecated("Use endInclusive instead.", ReplaceWith("endInclusive"))
    public override val end: Float get() = endInclusive

    override fun iterator(): FloatIterator = FloatProgressionIterator(start, endInclusive, increment)

    /** Checks if the progression is empty. */
    public open fun isEmpty(): Boolean = if (increment > 0) start > endInclusive else start < endInclusive

    override fun equals(other: Any?): Boolean =
        other is FloatProgression && (isEmpty() && other.isEmpty() ||
        java.lang.Float.compare(start, other.start) == 0 && java.lang.Float.compare(endInclusive, other.endInclusive) == 0 && java.lang.Float.compare(increment, other.increment) == 0)

    override fun hashCode(): Int =
        if (isEmpty()) -1 else (31 * (31 * java.lang.Float.floatToIntBits(start) + java.lang.Float.floatToIntBits(endInclusive)) + java.lang.Float.floatToIntBits(increment))

    override fun toString(): String = if (increment > 0) "$start..$endInclusive step $increment" else "$start downTo $endInclusive step ${-increment}"
}

/**
 * A progression of values of type `Double`.
 */
@Deprecated("This progression implementation has unclear semantics and will be removed soon.", level = DeprecationLevel.WARNING)
public open class DoubleProgression(
        override val start: Double,
                 val endInclusive: Double,
        override val increment: Double
) : Progression<Double> /*, Iterable<Double> */ {
    init {
        if (java.lang.Double.isNaN(increment)) throw IllegalArgumentException("Increment must be not NaN")
        if (increment == 0.0) throw IllegalArgumentException("Increment must be non-zero")
    }

    /**
     * The end value of the progression (inclusive).
     */
    @Deprecated("Use endInclusive instead.", ReplaceWith("endInclusive"))
    public override val end: Double get() = endInclusive

    override fun iterator(): DoubleIterator = DoubleProgressionIterator(start, endInclusive, increment)

    /** Checks if the progression is empty. */
    public open fun isEmpty(): Boolean = if (increment > 0) start > endInclusive else start < endInclusive

    override fun equals(other: Any?): Boolean =
        other is DoubleProgression && (isEmpty() && other.isEmpty() ||
        java.lang.Double.compare(start, other.start) == 0 && java.lang.Double.compare(endInclusive, other.endInclusive) == 0 && java.lang.Double.compare(increment, other.increment) == 0)

    override fun hashCode(): Int {
        if (isEmpty()) return -1
        var temp = java.lang.Double.doubleToLongBits(start)
        var result = (temp xor (temp ushr 32))
        temp = java.lang.Double.doubleToLongBits(endInclusive)
        result = 31 * result + (temp xor (temp ushr 32))
        temp = java.lang.Double.doubleToLongBits(increment)
        return (31 * result + (temp xor (temp ushr 32))).toInt()
    }

    override fun toString(): String = if (increment > 0) "$start..$endInclusive step $increment" else "$start downTo $endInclusive step ${-increment}"
}

