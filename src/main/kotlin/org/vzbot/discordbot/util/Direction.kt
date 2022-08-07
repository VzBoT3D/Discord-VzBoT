package org.vzbot.discordbot.util

enum class Direction(val unicode: String) {
    HOME("U+1F3E0") {
        override fun change(current: Point, min: Point, max: Point): Point {
            return Point(min.x, min.y)
        }

        override fun asGcode(to: Point, min: Point, max: Point, speed: Double): String {
            return "G0 X${min.x} Y${min.y} F${speed * 60}"
        }
    },

    RIGHT("U+27A1") {
        override fun change(current: Point, min: Point, max: Point): Point {
            return Point(max.x, current.y)
        }

        override fun asGcode(to: Point, min: Point, max: Point, speed: Double): String {
            return "G0 X${max.x} Y${to.y} F${speed * 60}"
        }
    },

    LEFT("U+2B05") {
        override fun change(current: Point, min: Point, max: Point): Point {
            return Point(min.x, current.y)
        }

        override fun asGcode(to: Point, min: Point, max: Point, speed: Double): String {
            return "G0 X${min.x} Y${to.y} F${speed * 60}"
        }
    },

    UP("U+2B06") {
        override fun change(current: Point, min: Point, max: Point): Point {
            return Point(current.x, max.y)
        }

        override fun asGcode(to: Point, min: Point, max: Point, speed: Double): String {
            return "G0 X${to.x} Y${max.y} F${speed * 60}"
        }
    },

    DOWN("U+2B07") {
        override fun change(current: Point, min: Point, max: Point): Point {
            return Point (current.x, min.y)
        }

        override fun asGcode(to: Point, min: Point, max: Point, speed: Double): String {
            return "G0 X${to.x} Y${min.y} F${speed * 60}"
        }
    },

    RIGHT_DOWN_LEFT("U+2198") {
        override fun change(current: Point, min: Point, max: Point): Point {
            return Point(max.x, min.y)
        }

        override fun asGcode(to: Point, min: Point, max: Point, speed: Double): String {
            return "G0 X${max.x} Y${min.y} F${speed * 60}"
        }
    },

    RIGHT_UP_LEFT("U+2197") {
        override fun change(current: Point, min: Point, max: Point): Point {
            return Point(max.x, max.y)
        }

        override fun asGcode(to: Point, min: Point, max: Point, speed: Double): String {
            return "G0 X${max.x} Y${max.y} F${speed * 60}"
        }
    },

    LEFT_DOWN_RIGHT("U+2199") {
        override fun change(current: Point, min: Point, max: Point): Point {
            return Point(min.x, min.y)
        }

        override fun asGcode(to: Point, min: Point, max: Point, speed: Double): String {
            return "G0 X${min.x} Y${min.y} F${speed * 60}"
        }
    },

    LEFT_UP_RIGHT("U+2196") {
        override fun change(current: Point, min: Point, max: Point): Point {
            return Point(min.x, max.y)
        }

        override fun asGcode(to: Point, min: Point, max: Point, speed: Double): String {
            return "G0 X${min.x} Y${max.y} F${speed * 60}"
        }
    };


    abstract fun change(current: Point, min: Point, max: Point): Point
    abstract fun asGcode(to: Point, min: Point, max: Point, speed: Double): String

}