<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <style type="text/css">
        :host,
        :root {
            --fa-font-solid: normal 900 1em/1 "Font Awesome 6 Solid";
            --fa-font-regular: normal 400 1em/1 "Font Awesome 6 Regular";
            --fa-font-light: normal 300 1em/1 "Font Awesome 6 Light";
            --fa-font-thin: normal 100 1em/1 "Font Awesome 6 Thin";
            --fa-font-duotone: normal 900 1em/1 "Font Awesome 6 Duotone";
            --fa-font-brands: normal 400 1em/1 "Font Awesome 6 Brands"
        }

        svg:not(:host).svg-inline--fa,
        svg:not(:root).svg-inline--fa {
            overflow: visible;
            box-sizing: content-box
        }

        .svg-inline--fa {
            display: var(--fa-display, inline-block);
            height: 1em;
            overflow: visible;
            vertical-align: -.125em
        }

        .svg-inline--fa.fa-2xs {
            vertical-align: .1em
        }

        .svg-inline--fa.fa-xs {
            vertical-align: 0
        }

        .svg-inline--fa.fa-sm {
            vertical-align: -.0714285705em
        }

        .svg-inline--fa.fa-lg {
            vertical-align: -.2em
        }

        .svg-inline--fa.fa-xl {
            vertical-align: -.25em
        }

        .svg-inline--fa.fa-2xl {
            vertical-align: -.3125em
        }

        .svg-inline--fa.fa-pull-left {
            margin-right: var(--fa-pull-margin, .3em);
            width: auto
        }

        .svg-inline--fa.fa-pull-right {
            margin-left: var(--fa-pull-margin, .3em);
            width: auto
        }

        .svg-inline--fa.fa-li {
            width: var(--fa-li-width, 2em);
            top: .25em
        }

        .svg-inline--fa.fa-fw {
            width: var(--fa-fw-width, 1.25em)
        }

        .fa-layers svg.svg-inline--fa {
            bottom: 0;
            left: 0;
            margin: auto;
            position: absolute;
            right: 0;
            top: 0
        }

        .fa-layers-counter,
        .fa-layers-text {
            display: inline-block;
            position: absolute;
            text-align: center
        }

        .fa-layers {
            display: inline-block;
            height: 1em;
            position: relative;
            text-align: center;
            vertical-align: -.125em;
            width: 1em
        }

        .fa-layers svg.svg-inline--fa {
            -webkit-transform-origin: center center;
            transform-origin: center center
        }

        .fa-layers-text {
            left: 50%;
            top: 50%;
            -webkit-transform: translate(-50%, -50%);
            transform: translate(-50%, -50%);
            -webkit-transform-origin: center center;
            transform-origin: center center
        }

        .fa-layers-counter {
            background-color: var(--fa-counter-background-color, #ff253a);
            border-radius: var(--fa-counter-border-radius, 1em);
            box-sizing: border-box;
            color: var(--fa-inverse, #fff);
            line-height: var(--fa-counter-line-height, 1);
            max-width: var(--fa-counter-max-width, 5em);
            min-width: var(--fa-counter-min-width, 1.5em);
            overflow: hidden;
            padding: var(--fa-counter-padding, .25em .5em);
            right: var(--fa-right, 0);
            text-overflow: ellipsis;
            top: var(--fa-top, 0);
            -webkit-transform: scale(var(--fa-counter-scale, .25));
            transform: scale(var(--fa-counter-scale, .25));
            -webkit-transform-origin: top right;
            transform-origin: top right
        }

        .fa-layers-bottom-right {
            bottom: var(--fa-bottom, 0);
            right: var(--fa-right, 0);
            top: auto;
            -webkit-transform: scale(var(--fa-layers-scale, .25));
            transform: scale(var(--fa-layers-scale, .25));
            -webkit-transform-origin: bottom right;
            transform-origin: bottom right
        }

        .fa-layers-bottom-left {
            bottom: var(--fa-bottom, 0);
            left: var(--fa-left, 0);
            right: auto;
            top: auto;
            -webkit-transform: scale(var(--fa-layers-scale, .25));
            transform: scale(var(--fa-layers-scale, .25));
            -webkit-transform-origin: bottom left;
            transform-origin: bottom left
        }

        .fa-layers-top-right {
            top: var(--fa-top, 0);
            right: var(--fa-right, 0);
            -webkit-transform: scale(var(--fa-layers-scale, .25));
            transform: scale(var(--fa-layers-scale, .25));
            -webkit-transform-origin: top right;
            transform-origin: top right
        }

        .fa-layers-top-left {
            left: var(--fa-left, 0);
            right: auto;
            top: var(--fa-top, 0);
            -webkit-transform: scale(var(--fa-layers-scale, .25));
            transform: scale(var(--fa-layers-scale, .25));
            -webkit-transform-origin: top left;
            transform-origin: top left
        }

        .fa-1x {
            font-size: 1em
        }

        .fa-2x {
            font-size: 2em
        }

        .fa-3x {
            font-size: 3em
        }

        .fa-4x {
            font-size: 4em
        }

        .fa-5x {
            font-size: 5em
        }

        .fa-6x {
            font-size: 6em
        }

        .fa-7x {
            font-size: 7em
        }

        .fa-8x {
            font-size: 8em
        }

        .fa-9x {
            font-size: 9em
        }

        .fa-10x {
            font-size: 10em
        }

        .fa-2xs {
            font-size: .625em;
            line-height: .1em;
            vertical-align: .225em
        }

        .fa-xs {
            font-size: .75em;
            line-height: .0833333337em;
            vertical-align: .125em
        }

        .fa-sm {
            font-size: .875em;
            line-height: .0714285718em;
            vertical-align: .0535714295em
        }

        .fa-lg {
            font-size: 1.25em;
            line-height: .05em;
            vertical-align: -.075em
        }

        .fa-xl {
            font-size: 1.5em;
            line-height: .0416666682em;
            vertical-align: -.125em
        }

        .fa-2xl {
            font-size: 2em;
            line-height: .03125em;
            vertical-align: -.1875em
        }

        .fa-fw {
            text-align: center;
            width: 1.25em
        }

        .fa-ul {
            list-style-type: none;
            margin-left: var(--fa-li-margin, 2.5em);
            padding-left: 0
        }

        .fa-ul > li {
            position: relative
        }

        .fa-li {
            left: calc(var(--fa-li-width, 2em) * -1);
            position: absolute;
            text-align: center;
            width: var(--fa-li-width, 2em);
            line-height: inherit
        }

        .fa-border {
            border-color: var(--fa-border-color, #eee);
            border-radius: var(--fa-border-radius, .1em);
            border-style: var(--fa-border-style, solid);
            border-width: var(--fa-border-width, .08em);
            padding: var(--fa-border-padding, .2em .25em .15em)
        }

        .fa-pull-left {
            float: left;
            margin-right: var(--fa-pull-margin, .3em)
        }

        .fa-pull-right {
            float: right;
            margin-left: var(--fa-pull-margin, .3em)
        }

        .fa-beat {
            -webkit-animation-name: fa-beat;
            animation-name: fa-beat;
            -webkit-animation-delay: var(--fa-animation-delay, 0);
            animation-delay: var(--fa-animation-delay, 0);
            -webkit-animation-direction: var(--fa-animation-direction, normal);
            animation-direction: var(--fa-animation-direction, normal);
            -webkit-animation-duration: var(--fa-animation-duration, 1s);
            animation-duration: var(--fa-animation-duration, 1s);
            -webkit-animation-iteration-count: var(--fa-animation-iteration-count, infinite);
            animation-iteration-count: var(--fa-animation-iteration-count, infinite);
            -webkit-animation-timing-function: var(--fa-animation-timing, ease-in-out);
            animation-timing-function: var(--fa-animation-timing, ease-in-out)
        }

        .fa-bounce {
            -webkit-animation-name: fa-bounce;
            animation-name: fa-bounce;
            -webkit-animation-delay: var(--fa-animation-delay, 0);
            animation-delay: var(--fa-animation-delay, 0);
            -webkit-animation-direction: var(--fa-animation-direction, normal);
            animation-direction: var(--fa-animation-direction, normal);
            -webkit-animation-duration: var(--fa-animation-duration, 1s);
            animation-duration: var(--fa-animation-duration, 1s);
            -webkit-animation-iteration-count: var(--fa-animation-iteration-count, infinite);
            animation-iteration-count: var(--fa-animation-iteration-count, infinite);
            -webkit-animation-timing-function: var(--fa-animation-timing, cubic-bezier(.28, .84, .42, 1));
            animation-timing-function: var(--fa-animation-timing, cubic-bezier(.28, .84, .42, 1))
        }

        .fa-fade {
            -webkit-animation-name: fa-fade;
            animation-name: fa-fade;
            -webkit-animation-delay: var(--fa-animation-delay, 0);
            animation-delay: var(--fa-animation-delay, 0);
            -webkit-animation-direction: var(--fa-animation-direction, normal);
            animation-direction: var(--fa-animation-direction, normal);
            -webkit-animation-duration: var(--fa-animation-duration, 1s);
            animation-duration: var(--fa-animation-duration, 1s);
            -webkit-animation-iteration-count: var(--fa-animation-iteration-count, infinite);
            animation-iteration-count: var(--fa-animation-iteration-count, infinite);
            -webkit-animation-timing-function: var(--fa-animation-timing, cubic-bezier(.4, 0, .6, 1));
            animation-timing-function: var(--fa-animation-timing, cubic-bezier(.4, 0, .6, 1))
        }

        .fa-beat-fade {
            -webkit-animation-name: fa-beat-fade;
            animation-name: fa-beat-fade;
            -webkit-animation-delay: var(--fa-animation-delay, 0);
            animation-delay: var(--fa-animation-delay, 0);
            -webkit-animation-direction: var(--fa-animation-direction, normal);
            animation-direction: var(--fa-animation-direction, normal);
            -webkit-animation-duration: var(--fa-animation-duration, 1s);
            animation-duration: var(--fa-animation-duration, 1s);
            -webkit-animation-iteration-count: var(--fa-animation-iteration-count, infinite);
            animation-iteration-count: var(--fa-animation-iteration-count, infinite);
            -webkit-animation-timing-function: var(--fa-animation-timing, cubic-bezier(.4, 0, .6, 1));
            animation-timing-function: var(--fa-animation-timing, cubic-bezier(.4, 0, .6, 1))
        }

        .fa-flip {
            -webkit-animation-name: fa-flip;
            animation-name: fa-flip;
            -webkit-animation-delay: var(--fa-animation-delay, 0);
            animation-delay: var(--fa-animation-delay, 0);
            -webkit-animation-direction: var(--fa-animation-direction, normal);
            animation-direction: var(--fa-animation-direction, normal);
            -webkit-animation-duration: var(--fa-animation-duration, 1s);
            animation-duration: var(--fa-animation-duration, 1s);
            -webkit-animation-iteration-count: var(--fa-animation-iteration-count, infinite);
            animation-iteration-count: var(--fa-animation-iteration-count, infinite);
            -webkit-animation-timing-function: var(--fa-animation-timing, ease-in-out);
            animation-timing-function: var(--fa-animation-timing, ease-in-out)
        }

        .fa-shake {
            -webkit-animation-name: fa-shake;
            animation-name: fa-shake;
            -webkit-animation-delay: var(--fa-animation-delay, 0);
            animation-delay: var(--fa-animation-delay, 0);
            -webkit-animation-direction: var(--fa-animation-direction, normal);
            animation-direction: var(--fa-animation-direction, normal);
            -webkit-animation-duration: var(--fa-animation-duration, 1s);
            animation-duration: var(--fa-animation-duration, 1s);
            -webkit-animation-iteration-count: var(--fa-animation-iteration-count, infinite);
            animation-iteration-count: var(--fa-animation-iteration-count, infinite);
            -webkit-animation-timing-function: var(--fa-animation-timing, linear);
            animation-timing-function: var(--fa-animation-timing, linear)
        }

        .fa-spin {
            -webkit-animation-name: fa-spin;
            animation-name: fa-spin;
            -webkit-animation-delay: var(--fa-animation-delay, 0);
            animation-delay: var(--fa-animation-delay, 0);
            -webkit-animation-direction: var(--fa-animation-direction, normal);
            animation-direction: var(--fa-animation-direction, normal);
            -webkit-animation-duration: var(--fa-animation-duration, 2s);
            animation-duration: var(--fa-animation-duration, 2s);
            -webkit-animation-iteration-count: var(--fa-animation-iteration-count, infinite);
            animation-iteration-count: var(--fa-animation-iteration-count, infinite);
            -webkit-animation-timing-function: var(--fa-animation-timing, linear);
            animation-timing-function: var(--fa-animation-timing, linear)
        }

        .fa-spin-reverse {
            --fa-animation-direction: reverse
        }

        .fa-pulse,
        .fa-spin-pulse {
            -webkit-animation-name: fa-spin;
            animation-name: fa-spin;
            -webkit-animation-direction: var(--fa-animation-direction, normal);
            animation-direction: var(--fa-animation-direction, normal);
            -webkit-animation-duration: var(--fa-animation-duration, 1s);
            animation-duration: var(--fa-animation-duration, 1s);
            -webkit-animation-iteration-count: var(--fa-animation-iteration-count, infinite);
            animation-iteration-count: var(--fa-animation-iteration-count, infinite);
            -webkit-animation-timing-function: var(--fa-animation-timing, steps(8));
            animation-timing-function: var(--fa-animation-timing, steps(8))
        }

        @media (prefers-reduced-motion: reduce) {

            .fa-beat,
            .fa-beat-fade,
            .fa-bounce,
            .fa-fade,
            .fa-flip,
            .fa-pulse,
            .fa-shake,
            .fa-spin,
            .fa-spin-pulse {
                -webkit-animation-delay: -1ms;
                animation-delay: -1ms;
                -webkit-animation-duration: 1ms;
                animation-duration: 1ms;
                -webkit-animation-iteration-count: 1;
                animation-iteration-count: 1;
                transition-delay: 0s;
                transition-duration: 0s
            }
        }

        @-webkit-keyframes fa-beat {

            0%,
            90% {
                -webkit-transform: scale(1);
                transform: scale(1)
            }

            45% {
                -webkit-transform: scale(var(--fa-beat-scale, 1.25));
                transform: scale(var(--fa-beat-scale, 1.25))
            }
        }

        @keyframes fa-beat {

            0%,
            90% {
                -webkit-transform: scale(1);
                transform: scale(1)
            }

            45% {
                -webkit-transform: scale(var(--fa-beat-scale, 1.25));
                transform: scale(var(--fa-beat-scale, 1.25))
            }
        }

        @-webkit-keyframes fa-bounce {
            0% {
                -webkit-transform: scale(1, 1) translateY(0);
                transform: scale(1, 1) translateY(0)
            }

            10% {
                -webkit-transform: scale(var(--fa-bounce-start-scale-x, 1.1), var(--fa-bounce-start-scale-y, .9)) translateY(0);
                transform: scale(var(--fa-bounce-start-scale-x, 1.1), var(--fa-bounce-start-scale-y, .9)) translateY(0)
            }

            30% {
                -webkit-transform: scale(var(--fa-bounce-jump-scale-x, .9), var(--fa-bounce-jump-scale-y, 1.1)) translateY(var(--fa-bounce-height, -.5em));
                transform: scale(var(--fa-bounce-jump-scale-x, .9), var(--fa-bounce-jump-scale-y, 1.1)) translateY(var(--fa-bounce-height, -.5em))
            }

            50% {
                -webkit-transform: scale(var(--fa-bounce-land-scale-x, 1.05), var(--fa-bounce-land-scale-y, .95)) translateY(0);
                transform: scale(var(--fa-bounce-land-scale-x, 1.05), var(--fa-bounce-land-scale-y, .95)) translateY(0)
            }

            57% {
                -webkit-transform: scale(1, 1) translateY(var(--fa-bounce-rebound, -.125em));
                transform: scale(1, 1) translateY(var(--fa-bounce-rebound, -.125em))
            }

            64% {
                -webkit-transform: scale(1, 1) translateY(0);
                transform: scale(1, 1) translateY(0)
            }

            100% {
                -webkit-transform: scale(1, 1) translateY(0);
                transform: scale(1, 1) translateY(0)
            }
        }

        @keyframes fa-bounce {
            0% {
                -webkit-transform: scale(1, 1) translateY(0);
                transform: scale(1, 1) translateY(0)
            }

            10% {
                -webkit-transform: scale(var(--fa-bounce-start-scale-x, 1.1), var(--fa-bounce-start-scale-y, .9)) translateY(0);
                transform: scale(var(--fa-bounce-start-scale-x, 1.1), var(--fa-bounce-start-scale-y, .9)) translateY(0)
            }

            30% {
                -webkit-transform: scale(var(--fa-bounce-jump-scale-x, .9), var(--fa-bounce-jump-scale-y, 1.1)) translateY(var(--fa-bounce-height, -.5em));
                transform: scale(var(--fa-bounce-jump-scale-x, .9), var(--fa-bounce-jump-scale-y, 1.1)) translateY(var(--fa-bounce-height, -.5em))
            }

            50% {
                -webkit-transform: scale(var(--fa-bounce-land-scale-x, 1.05), var(--fa-bounce-land-scale-y, .95)) translateY(0);
                transform: scale(var(--fa-bounce-land-scale-x, 1.05), var(--fa-bounce-land-scale-y, .95)) translateY(0)
            }

            57% {
                -webkit-transform: scale(1, 1) translateY(var(--fa-bounce-rebound, -.125em));
                transform: scale(1, 1) translateY(var(--fa-bounce-rebound, -.125em))
            }

            64% {
                -webkit-transform: scale(1, 1) translateY(0);
                transform: scale(1, 1) translateY(0)
            }

            100% {
                -webkit-transform: scale(1, 1) translateY(0);
                transform: scale(1, 1) translateY(0)
            }
        }

        @-webkit-keyframes fa-fade {
            50% {
                opacity: var(--fa-fade-opacity, .4)
            }
        }

        @keyframes fa-fade {
            50% {
                opacity: var(--fa-fade-opacity, .4)
            }
        }

        @-webkit-keyframes fa-beat-fade {

            0%,
            100% {
                opacity: var(--fa-beat-fade-opacity, .4);
                -webkit-transform: scale(1);
                transform: scale(1)
            }

            50% {
                opacity: 1;
                -webkit-transform: scale(var(--fa-beat-fade-scale, 1.125));
                transform: scale(var(--fa-beat-fade-scale, 1.125))
            }
        }

        @keyframes fa-beat-fade {

            0%,
            100% {
                opacity: var(--fa-beat-fade-opacity, .4);
                -webkit-transform: scale(1);
                transform: scale(1)
            }

            50% {
                opacity: 1;
                -webkit-transform: scale(var(--fa-beat-fade-scale, 1.125));
                transform: scale(var(--fa-beat-fade-scale, 1.125))
            }
        }

        @-webkit-keyframes fa-flip {
            50% {
                -webkit-transform: rotate3d(var(--fa-flip-x, 0), var(--fa-flip-y, 1), var(--fa-flip-z, 0), var(--fa-flip-angle, -180deg));
                transform: rotate3d(var(--fa-flip-x, 0), var(--fa-flip-y, 1), var(--fa-flip-z, 0), var(--fa-flip-angle, -180deg))
            }
        }

        @keyframes fa-flip {
            50% {
                -webkit-transform: rotate3d(var(--fa-flip-x, 0), var(--fa-flip-y, 1), var(--fa-flip-z, 0), var(--fa-flip-angle, -180deg));
                transform: rotate3d(var(--fa-flip-x, 0), var(--fa-flip-y, 1), var(--fa-flip-z, 0), var(--fa-flip-angle, -180deg))
            }
        }

        @-webkit-keyframes fa-shake {
            0% {
                -webkit-transform: rotate(-15deg);
                transform: rotate(-15deg)
            }

            4% {
                -webkit-transform: rotate(15deg);
                transform: rotate(15deg)
            }

            24%,
            8% {
                -webkit-transform: rotate(-18deg);
                transform: rotate(-18deg)
            }

            12%,
            28% {
                -webkit-transform: rotate(18deg);
                transform: rotate(18deg)
            }

            16% {
                -webkit-transform: rotate(-22deg);
                transform: rotate(-22deg)
            }

            20% {
                -webkit-transform: rotate(22deg);
                transform: rotate(22deg)
            }

            32% {
                -webkit-transform: rotate(-12deg);
                transform: rotate(-12deg)
            }

            36% {
                -webkit-transform: rotate(12deg);
                transform: rotate(12deg)
            }

            100%,
            40% {
                -webkit-transform: rotate(0);
                transform: rotate(0)
            }
        }

        @keyframes fa-shake {
            0% {
                -webkit-transform: rotate(-15deg);
                transform: rotate(-15deg)
            }

            4% {
                -webkit-transform: rotate(15deg);
                transform: rotate(15deg)
            }

            24%,
            8% {
                -webkit-transform: rotate(-18deg);
                transform: rotate(-18deg)
            }

            12%,
            28% {
                -webkit-transform: rotate(18deg);
                transform: rotate(18deg)
            }

            16% {
                -webkit-transform: rotate(-22deg);
                transform: rotate(-22deg)
            }

            20% {
                -webkit-transform: rotate(22deg);
                transform: rotate(22deg)
            }

            32% {
                -webkit-transform: rotate(-12deg);
                transform: rotate(-12deg)
            }

            36% {
                -webkit-transform: rotate(12deg);
                transform: rotate(12deg)
            }

            100%,
            40% {
                -webkit-transform: rotate(0);
                transform: rotate(0)
            }
        }

        @-webkit-keyframes fa-spin {
            0% {
                -webkit-transform: rotate(0);
                transform: rotate(0)
            }

            100% {
                -webkit-transform: rotate(360deg);
                transform: rotate(360deg)
            }
        }

        @keyframes fa-spin {
            0% {
                -webkit-transform: rotate(0);
                transform: rotate(0)
            }

            100% {
                -webkit-transform: rotate(360deg);
                transform: rotate(360deg)
            }
        }

        .fa-rotate-90 {
            -webkit-transform: rotate(90deg);
            transform: rotate(90deg)
        }

        .fa-rotate-180 {
            -webkit-transform: rotate(180deg);
            transform: rotate(180deg)
        }

        .fa-rotate-270 {
            -webkit-transform: rotate(270deg);
            transform: rotate(270deg)
        }

        .fa-flip-horizontal {
            -webkit-transform: scale(-1, 1);
            transform: scale(-1, 1)
        }

        .fa-flip-vertical {
            -webkit-transform: scale(1, -1);
            transform: scale(1, -1)
        }

        .fa-flip-both,
        .fa-flip-horizontal.fa-flip-vertical {
            -webkit-transform: scale(-1, -1);
            transform: scale(-1, -1)
        }

        .fa-rotate-by {
            -webkit-transform: rotate(var(--fa-rotate-angle, none));
            transform: rotate(var(--fa-rotate-angle, none))
        }

        .fa-stack {
            display: inline-block;
            vertical-align: middle;
            height: 2em;
            position: relative;
            width: 2.5em
        }

        .fa-stack-1x,
        .fa-stack-2x {
            bottom: 0;
            left: 0;
            margin: auto;
            position: absolute;
            right: 0;
            top: 0;
            z-index: var(--fa-stack-z-index, auto)
        }

        .svg-inline--fa.fa-stack-1x {
            height: 1em;
            width: 1.25em
        }

        .svg-inline--fa.fa-stack-2x {
            height: 2em;
            width: 2.5em
        }

        .fa-inverse {
            color: var(--fa-inverse, #fff)
        }

        .fa-sr-only,
        .sr-only {
            position: absolute;
            width: 1px;
            height: 1px;
            padding: 0;
            margin: -1px;
            overflow: hidden;
            clip: rect(0, 0, 0, 0);
            white-space: nowrap;
            border-width: 0
        }

        .fa-sr-only-focusable:not(:focus),
        .sr-only-focusable:not(:focus) {
            position: absolute;
            width: 1px;
            height: 1px;
            padding: 0;
            margin: -1px;
            overflow: hidden;
            clip: rect(0, 0, 0, 0);
            white-space: nowrap;
            border-width: 0
        }

        .svg-inline--fa .fa-primary {
            fill: var(--fa-primary-color, currentColor);
            opacity: var(--fa-primary-opacity, 1)
        }

        .svg-inline--fa .fa-secondary {
            fill: var(--fa-secondary-color, currentColor);
            opacity: var(--fa-secondary-opacity, .4)
        }

        .svg-inline--fa.fa-swap-opacity .fa-primary {
            opacity: var(--fa-secondary-opacity, .4)
        }

        .svg-inline--fa.fa-swap-opacity .fa-secondary {
            opacity: var(--fa-primary-opacity, 1)
        }

        .svg-inline--fa mask .fa-primary,
        .svg-inline--fa mask .fa-secondary {
            fill: #000
        }

        .fa-duotone.fa-inverse,
        .fad.fa-inverse {
            color: var(--fa-inverse, #fff)
        }
    </style>
    <style>
        .anticon {
            display: inline-block;
            color: inherit;
            font-style: normal;
            line-height: 0;
            text-align: center;
            text-transform: none;
            vertical-align: -0.125em;
            text-rendering: optimizeLegibility;
            -webkit-font-smoothing: antialiased;
            -moz-osx-font-smoothing: grayscale;
        }

        .anticon > * {
            line-height: 1;
        }

        .anticon svg {
            display: inline-block;
        }

        .anticon::before {
            display: none;
        }

        .anticon .anticon-icon {
            display: block;
        }

        .anticon[tabindex] {
            cursor: pointer;
        }

        .anticon-spin::before,
        .anticon-spin {
            display: inline-block;
            -webkit-animation: loadingCircle 1s infinite linear;
            animation: loadingCircle 1s infinite linear;
        }

        @-webkit-keyframes loadingCircle {
            100% {
                -webkit-transform: rotate(360deg);
                transform: rotate(360deg);
            }
        }

        @keyframes loadingCircle {
            100% {
                -webkit-transform: rotate(360deg);
                transform: rotate(360deg);
            }
        }
    </style>
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>EFAK - TV</title>
    <style>
        @-webkit-keyframes square-spin {
            25% {
                -webkit-transform: perspective(100px) rotateX(180deg) rotateY(0);
                transform: perspective(100px) rotateX(180deg) rotateY(0);
            }

            50% {
                -webkit-transform: perspective(100px) rotateX(180deg) rotateY(180deg);
                transform: perspective(100px) rotateX(180deg) rotateY(180deg);
            }

            75% {
                -webkit-transform: perspective(100px) rotateX(0) rotateY(180deg);
                transform: perspective(100px) rotateX(0) rotateY(180deg);
            }


            100% {
                -webkit-transform: perspective(100px) rotateX(0) rotateY(0);
                transform: perspective(100px) rotateX(0) rotateY(0);
            }
        }

        @keyframes square-spin {
            25% {
                -webkit-transform: perspective(100px) rotateX(180deg) rotateY(0);
                transform: perspective(100px) rotateX(180deg) rotateY(0);
            }

            50% {
                -webkit-transform: perspective(100px) rotateX(180deg) rotateY(180deg);
                transform: perspective(100px) rotateX(180deg) rotateY(180deg);
            }

            75% {
                -webkit-transform: perspective(100px) rotateX(0) rotateY(180deg);
                transform: perspective(100px) rotateX(0) rotateY(180deg);
            }

            100% {
                -webkit-transform: perspective(100px) rotateX(0) rotateY(0);
                transform: perspective(100px) rotateX(0) rotateY(0);
            }
        }

        .square-spin > div {
            -webkit-animation: square-spin 3s 0s cubic-bezier(0.09, 0.57, 0.49, 0.9) infinite;
            animation: square-spin 3s 0s cubic-bezier(0.09, 0.57, 0.49, 0.9) infinite;
            -webkit-animation-fill-mode: both;
            animation-fill-mode: both;
            width: 50px;
            height: 50px;
            background: #06f;
        }

        .square-spin {
            display: flex;
            justify-content: center;
            position: relative;
            top: 200px;
        }
    </style>
    <link rel="shortcut icon" href="/media/img/favicon.ico"/>
    <link href="/media/css/tv/css/tv-common.css" rel="stylesheet">
    <link href="/media/css/tv/css/tv-playground.css" rel="stylesheet">
    <link href="/media/css/tv/css/tv-vendors.css" rel="stylesheet">

    <style type="text/css">
        #dv-full-screen-container {
            position: fixed;
            top: 0px;
            left: 0px;
            overflow: hidden;
            transform-origin: left top;
            z-index: 999;
        }
    </style>
    <style type="text/css">
        .dv-loading {
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }

        .dv-loading .loading-tip {
            font-size: 15px;
        }
    </style>
    <style type="text/css">
        .dv-border-box-1 {
            position: relative;
            width: 100%;
            height: 100%;
        }

        .dv-border-box-1 .border {
            position: absolute;
            display: block;
        }

        .dv-border-box-1 .right-top {
            right: 0px;
            transform: rotateY(180deg);
        }

        .dv-border-box-1 .left-bottom {
            bottom: 0px;
            transform: rotateX(180deg);
        }

        .dv-border-box-1 .right-bottom {
            right: 0px;
            bottom: 0px;
            transform: rotateX(180deg) rotateY(180deg);
        }

        .dv-border-box-1 .border-box-content {
            position: relative;
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-border-box-2 {
            position: relative;
            width: 100%;
            height: 100%;
        }

        .dv-border-box-2 .dv-border-svg-container {
            position: absolute;
            width: 100%;
            height: 100%;
            top: 0px;
            left: 0px;
        }

        .dv-border-box-2 .dv-border-svg-container > polyline {
            fill: none;
            stroke-width: 1;
        }

        .dv-border-box-2 .border-box-content {
            position: relative;
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-border-box-3 {
            position: relative;
            width: 100%;
            height: 100%;
        }

        .dv-border-box-3 .dv-border-svg-container {
            position: absolute;
            width: 100%;
            height: 100%;
            top: 0px;
            left: 0px;
        }

        .dv-border-box-3 .dv-border-svg-container > polyline {
            fill: none;
        }

        .dv-border-box-3 .dv-bb3-line1 {
            stroke-width: 3;
        }

        .dv-border-box-3 .dv-bb3-line2 {
            stroke-width: 1;
        }

        .dv-border-box-3 .border-box-content {
            position: relative;
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-border-box-4 {
            position: relative;
            width: 100%;
            height: 100%;
        }

        .dv-border-box-4 .dv-reverse {
            transform: rotate(180deg);
        }

        .dv-border-box-4 .dv-border-svg-container {
            position: absolute;
            width: 100%;
            height: 100%;
            top: 0px;
            left: 0px;
        }

        .dv-border-box-4 .dv-border-svg-container > polyline {
            fill: none;
        }

        .dv-border-box-4 .sw1 {
            stroke-width: 1;
        }

        .dv-border-box-4 .sw3 {
            stroke-width: 3px;
            stroke-linecap: round;
        }

        .dv-border-box-4 .dv-bb4-line-1 {
            stroke-width: 1;
        }

        .dv-border-box-4 .dv-bb4-line-2 {
            stroke-width: 1;
        }

        .dv-border-box-4 .dv-bb4-line-3 {
            stroke-width: 3px;
            stroke-linecap: round;
        }

        .dv-border-box-4 .dv-bb4-line-4 {
            stroke-width: 3px;
            stroke-linecap: round;
        }

        .dv-border-box-4 .dv-bb4-line-5 {
            stroke-width: 1;
        }

        .dv-border-box-4 .dv-bb4-line-6 {
            stroke-width: 1;
        }

        .dv-border-box-4 .dv-bb4-line-7 {
            stroke-width: 1;
        }

        .dv-border-box-4 .dv-bb4-line-8 {
            stroke-width: 3px;
            stroke-linecap: round;
        }

        .dv-border-box-4 .dv-bb4-line-9 {
            stroke-width: 3px;
            stroke-linecap: round;
            stroke-dasharray: 100 250;
        }

        .dv-border-box-4 .dv-bb4-line-10 {
            stroke-width: 1;
            stroke-dasharray: 80 270;
        }

        .dv-border-box-4 .border-box-content {
            position: relative;
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-border-box-5 {
            position: relative;
            width: 100%;
            height: 100%;
        }

        .dv-border-box-5 .dv-reverse {
            transform: rotate(180deg);
        }

        .dv-border-box-5 .dv-border-svg-container {
            position: absolute;
            top: 0px;
            left: 0px;
            width: 100%;
            height: 100%;
        }

        .dv-border-box-5 .dv-border-svg-container > polyline {
            fill: none;
        }

        .dv-border-box-5 .dv-bb5-line-1,
        .dv-border-box-5 .dv-bb5-line-2 {
            stroke-width: 1;
        }

        .dv-border-box-5 .dv-bb5-line-3,
        .dv-border-box-5 .dv-bb5-line-6 {
            stroke-width: 5;
        }

        .dv-border-box-5 .dv-bb5-line-4,
        .dv-border-box-5 .dv-bb5-line-5 {
            stroke-width: 2;
        }

        .dv-border-box-5 .border-box-content {
            position: relative;
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-border-box-6 {
            position: relative;
            width: 100%;
            height: 100%;
        }

        .dv-border-box-6 .dv-border-svg-container {
            position: absolute;
            top: 0px;
            left: 0px;
            width: 100%;
            height: 100%;
        }

        .dv-border-box-6 .dv-border-svg-container > polyline {
            fill: none;
            stroke-width: 1;
        }

        .dv-border-box-6 .border-box-content {
            position: relative;
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-border-box-7 {
            position: relative;
            width: 100%;
            height: 100%;
        }

        .dv-border-box-7 .dv-border-svg-container {
            position: absolute;
            top: 0px;
            left: 0px;
            width: 100%;
            height: 100%;
        }

        .dv-border-box-7 .dv-border-svg-container > polyline {
            fill: none;
            stroke-linecap: round;
        }

        .dv-border-box-7 .dv-bb7-line-width-2 {
            stroke-width: 2;
        }

        .dv-border-box-7 .dv-bb7-line-width-5 {
            stroke-width: 5;
        }

        .dv-border-box-7 .border-box-content {
            position: relative;
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-border-box-8 {
            position: relative;
            width: 100%;
            height: 100%;
        }

        .dv-border-box-8 .dv-border-svg-container {
            position: absolute;
            width: 100%;
            height: 100%;
            left: 0px;
            top: 0px;
        }

        .dv-border-box-8 .border-box-content {
            position: relative;
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-border-box-9 {
            position: relative;
            width: 100%;
            height: 100%;
        }

        .dv-border-box-9 .dv-border-svg-container {
            position: absolute;
            width: 100%;
            height: 100%;
            left: 0px;
            top: 0px;
        }

        .dv-border-box-9 .border-box-content {
            position: relative;
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-border-box-10 {
            position: relative;
            width: 100%;
            height: 100%;
            border-radius: 6px;
        }

        .dv-border-box-10 .dv-border-svg-container {
            position: absolute;
            display: block;
        }

        .dv-border-box-10 .right-top {
            right: 0px;
            transform: rotateY(180deg);
        }

        .dv-border-box-10 .left-bottom {
            bottom: 0px;
            transform: rotateX(180deg);
        }

        .dv-border-box-10 .right-bottom {
            right: 0px;
            bottom: 0px;
            transform: rotateX(180deg) rotateY(180deg);
        }

        .dv-border-box-10 .border-box-content {
            position: relative;
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-border-box-11 {
            position: relative;
            width: 100%;
            height: 100%;
        }

        .dv-border-box-11 .dv-border-svg-container {
            position: absolute;
            width: 100%;
            height: 100%;
            top: 0px;
            left: 0px;
        }

        .dv-border-box-11 .dv-border-svg-container > polyline {
            fill: none;
            stroke-width: 1;
        }

        .dv-border-box-11 .border-box-content {
            position: relative;
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-border-box-12 {
            position: relative;
            width: 100%;
            height: 100%;
        }

        .dv-border-box-12 .dv-border-svg-container {
            position: absolute;
            width: 100%;
            height: 100%;
            top: 0px;
            left: 0px;
        }

        .dv-border-box-12 .border-box-content {
            position: relative;
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-border-box-13 {
            position: relative;
            width: 100%;
            height: 100%;
        }

        .dv-border-box-13 .dv-border-svg-container {
            position: absolute;
            width: 100%;
            height: 100%;
            top: 0px;
            left: 0px;
        }

        .dv-border-box-13 .border-box-content {
            position: relative;
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-decoration-1 {
            width: 100%;
            height: 100%;
        }

        .dv-decoration-1 svg {
            transform-origin: left top;
        }
    </style>
    <style type="text/css">
        .dv-decoration-2 {
            display: flex;
            width: 100%;
            height: 100%;
            justify-content: center;
            align-items: center;
        }
    </style>
    <style type="text/css">
        .dv-decoration-3 {
            width: 100%;
            height: 100%;
        }

        .dv-decoration-3 svg {
            transform-origin: left top;
        }
    </style>
    <style type="text/css">
        .dv-decoration-4 {
            position: relative;
            width: 100%;
            height: 100%;
        }

        .dv-decoration-4 .container {
            display: flex;
            overflow: hidden;
            position: absolute;
            flex: 1;
        }

        .dv-decoration-4 .normal {
            animation: ani-height ease-in-out infinite;
            left: 50%;
            margin-left: -2px;
        }

        .dv-decoration-4 .reverse {
            animation: ani-width ease-in-out infinite;
            top: 50%;
            margin-top: -2px;
        }

        @keyframes ani-height {
            0% {
                height: 0%;
            }

            70% {
                height: 100%;
            }

            100% {
                height: 100%;
            }
        }

        @keyframes ani-width {
            0% {
                width: 0%;
            }

            70% {
                width: 100%;
            }

            100% {
                width: 100%;
            }
        }
    </style>
    <style type="text/css">
        .dv-decoration-5 {
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-decoration-6 {
            width: 100%;
            height: 100%;
        }

        .dv-decoration-6 svg {
            transform-origin: left top;
        }
    </style>
    <style type="text/css">
        .dv-decoration-7 {
            display: flex;
            width: 100%;
            height: 100%;
            justify-content: center;
            align-items: center;
        }
    </style>
    <style type="text/css">
        .dv-decoration-8 {
            display: flex;
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-decoration-9 {
            position: relative;
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .dv-decoration-9 svg {
            position: absolute;
            left: 0px;
            top: 0px;
            transform-origin: left top;
        }
    </style>
    <style type="text/css">
        .dv-decoration-10 {
            width: 100%;
            height: 100%;
            display: flex;
        }
    </style>
    <style type="text/css">
        .dv-decoration-11 {
            position: relative;
            width: 100%;
            height: 100%;
            display: flex;
        }

        .dv-decoration-11 .decoration-content {
            position: absolute;
            top: 0px;
            left: 0px;
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
        }
    </style>
    <style type="text/css">
        .dv-decoration-12 {
            position: relative;
            width: 100%;
            height: 100%;
            display: flex;
        }

        .dv-decoration-12 .decoration-content {
            position: absolute;
            top: 0px;
            left: 0px;
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
        }
    </style>
    <style type="text/css">
        .dv-charts-container {
            position: relative;
            width: 100%;
            height: 100%;
        }

        .dv-charts-container .charts-canvas-container {
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-digital-flop canvas {
            width: 100%;
            height: 100%;
        }
    </style>
    <style type="text/css">
        .dv-active-ring-chart {
            position: relative;
        }

        .dv-active-ring-chart .active-ring-chart-container {
            width: 100%;
            height: 100%;
        }

        .dv-active-ring-chart .active-ring-info {
            position: absolute;
            width: 100%;
            height: 100%;
            left: 0px;
            top: 0px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }

        .dv-active-ring-chart .active-ring-info .dv-digital-flop {
            width: 100px;
            height: 30px;
        }

        .dv-active-ring-chart .active-ring-info .active-ring-name {
            width: 100px;
            height: 30px;
            color: #fff;
            text-align: center;
            vertical-align: middle;
            text-overflow: ellipsis;
            overflow: hidden;
            white-space: nowrap;
        }
    </style>
    <style type="text/css">
        .dv-capsule-chart {
            position: relative;
            display: flex;
            flex-direction: row;
            box-sizing: border-box;
            padding: 10px;
            color: #fff;
        }

        .dv-capsule-chart .label-column {
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            box-sizing: border-box;
            padding-right: 10px;
            text-align: right;
            font-size: 12px;
        }

        .dv-capsule-chart .label-column div {
            height: 20px;
            line-height: 20px;
        }

        .dv-capsule-chart .capsule-container {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }

        .dv-capsule-chart .capsule-item {
            box-shadow: 0 0 3px #999;
            height: 10px;
            margin: 5px 0px;
            border-radius: 5px;
        }

        .dv-capsule-chart .capsule-item .capsule-item-column {
            position: relative;
            height: 8px;
            margin-top: 1px;
            border-radius: 5px;
            transition: all 0.3s;
            display: flex;
            justify-content: flex-end;
            align-items: center;
        }

        .dv-capsule-chart .capsule-item .capsule-item-column .capsule-item-value {
            font-size: 12px;
            transform: translateX(100%);
        }

        .dv-capsule-chart .unit-label {
            height: 20px;
            font-size: 12px;
            position: relative;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .dv-capsule-chart .unit-text {
            text-align: right;
            display: flex;
            align-items: flex-end;
            font-size: 12px;
            line-height: 20px;
            margin-left: 10px;
        }
    </style>
    <style type="text/css">
        .dv-water-pond-level {
            position: relative;
        }

        .dv-water-pond-level svg {
            position: absolute;
            width: 100%;
            height: 100%;
            top: 0px;
            left: 0px;
        }

        .dv-water-pond-level text {
            font-size: 25px;
            font-weight: bold;
            text-anchor: middle;
            dominant-baseline: middle;
        }

        .dv-water-pond-level ellipse,
        .dv-water-pond-level rect {
            fill: none;
            stroke-width: 3;
        }

        .dv-water-pond-level canvas {
            margin-top: 8px;
            margin-left: 8px;
            width: calc(100% - 16px);
            height: calc(100% - 16px);
            box-sizing: border-box;
        }
    </style>
    <style type="text/css">
        .dv-percent-pond {
            position: relative;
            display: flex;
            flex-direction: column;
        }

        .dv-percent-pond svg {
            position: absolute;
            left: 0px;
            top: 0px;
            width: 100%;
            height: 100%;
        }

        .dv-percent-pond polyline {
            transition: all 0.3s;
        }

        .dv-percent-pond text {
            font-size: 25px;
            font-weight: bold;
            text-anchor: middle;
            dominant-baseline: middle;
        }
    </style>
    <style type="text/css">
        .dv-flyline-chart {
            display: flex;
            flex-direction: column;
            background-size: 100% 100%;
        }

        .dv-flyline-chart polyline {
            transition: all 0.3s;
        }

        .dv-flyline-chart text {
            text-anchor: middle;
            dominant-baseline: middle;
        }
    </style>
    <style type="text/css">
        .dv-flyline-chart-enhanced {
            display: flex;
            flex-direction: column;
            background-size: 100% 100%;
        }

        .dv-flyline-chart-enhanced text {
            text-anchor: middle;
            dominant-baseline: middle;
        }
    </style>
    <style type="text/css">
        .dv-conical-column-chart {
            width: 100%;
            height: 100%;
        }

        .dv-conical-column-chart text {
            text-anchor: middle;
        }
    </style>
    <style type="text/css">
        .dv-scroll-board {
            position: relative;
            width: 100%;
            height: 100%;
            color: #fff;
        }

        .dv-scroll-board .text {
            padding: 0 10px;
            box-sizing: border-box;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .dv-scroll-board .header {
            display: flex;
            flex-direction: row;
            font-size: 15px;
        }

        .dv-scroll-board .header .header-item {
            padding: 0 10px;
            box-sizing: border-box;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            transition: all 0.3s;
        }

        .dv-scroll-board .rows {
            overflow: hidden;
        }

        .dv-scroll-board .rows .row-item {
            display: flex;
            font-size: 14px;
            transition: all 0.3s;
        }

        .dv-scroll-board .rows .ceil {
            padding: 0 10px;
            box-sizing: border-box;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .dv-scroll-board .rows .index {
            border-radius: 3px;
            padding: 0px 3px;
        }
    </style>
    <style type="text/css">
        .dv-scroll-ranking-board {
            width: 100%;
            height: 100%;
            color: #fff;
            overflow: hidden;
        }

        .dv-scroll-ranking-board .row-item {
            transition: all 0.3s;
            display: flex;
            flex-direction: column;
            justify-content: center;
            overflow: hidden;
        }

        .dv-scroll-ranking-board .ranking-info {
            display: flex;
            width: 100%;
            font-size: 13px;
        }

        .dv-scroll-ranking-board .ranking-info .rank {
            width: 40px;
            color: #1370fb;
        }

        .dv-scroll-ranking-board .ranking-info .info-name {
            flex: 1;
        }

        .dv-scroll-ranking-board .ranking-column {
            border-bottom: 2px solid rgba(19, 112, 251, 0.5);
            margin-top: 5px;
        }

        .dv-scroll-ranking-board .ranking-column .inside-column {
            position: relative;
            height: 6px;
            background-color: #1370fb;
            margin-bottom: 2px;
            border-radius: 1px;
            overflow: hidden;
        }

        .dv-scroll-ranking-board .ranking-column .shine {
            position: absolute;
            left: 0%;
            top: 2px;
            height: 2px;
            width: 50px;
            transform: translateX(-100%);
            background: radial-gradient(#28f8ff 5%, transparent 80%);
            animation: shine 3s ease-in-out infinite alternate;
        }

        @keyframes shine {
            80% {
                left: 0%;
                transform: translateX(-100%);
            }

            100% {
                left: 100%;
                transform: translateX(0%);
            }
        }
    </style>
</head>

<body class="efak-dashboard" style="background: rgb(240, 242, 245); cursor: default;">
<svg aria-hidden="true"
     style="position: absolute; width: 0px; height: 0px; overflow: hidden;">
    <symbol id="iconnav-tvMode" viewBox="0 0 1024 1024">
        <path
                d="M657.728 556.8h-3.2L579.776 321.408c-7.04-22.976-18.048-33.472-35.52-33.472-19.52 0-33.728 12.48-33.728 30.528 0 6.976 1.92 16.704 5.44 26.944l84.288 242.56c11.008 32.704 27.264 46.976 55.488 46.976 27.776 0 43.52-14.016 54.528-47.04l84.48-242.496c3.008-9.216 5.248-20.48 5.248-27.2 0-18.048-14.016-30.272-33.28-30.272-17.92 0-28.16 9.728-35.2 33.472L657.728 556.8zM344.512 635.008c20.736 0 33.728-13.76 33.728-36.032V348.032h72.512c17.024 0 28.992-11.776 28.992-28.288 0-16.768-11.968-28.48-28.992-28.48H238.528a27.84 27.84 0 0 0-29.056 28.48c0 16.256 12.288 28.224 29.056 28.224h72.192V599.04c0 22.528 12.8 36.032 33.792 36.032z">
        </path>
        <path
                d="M128 64a64 64 0 0 0-64 64v640a64 64 0 0 0 64 64h768a64 64 0 0 0 64-64V128a64 64 0 0 0-64-64H128z m768 64v640H128V128h768zM192 896a32 32 0 0 0 0 64h640a32 32 0 0 0 0-64H192z">
        </path>
    </symbol>
    <symbol id="iconnav-rightTriangle" viewBox="0 0 1024 1024">
        <path
                d="M442.048 773.952c-40.32 40.32-109.248 11.776-109.248-45.248V268.8c0-56.96 68.928-85.568 109.248-45.248L672 453.504a64 64 0 0 1 0 90.496l-229.952 229.952z">
        </path>
    </symbol>
    <symbol id="iconnav-leftTriangle" viewBox="0 0 1024 1024">
        <path
                d="M581.504 223.552c40.32-40.32 109.248-11.776 109.248 45.248v459.904c0 56.96-68.928 85.568-109.248 45.248L351.552 544a64 64 0 0 1 0-90.496l229.952-229.952z">
        </path>
    </symbol>
    <symbol id="iconnav-clusterDiagnostics" viewBox="0 0 1024 1024">
        <path
                d="M64 128a64 64 0 0 1 64-64h768a64 64 0 0 1 64 64v768a64 64 0 0 1-64 64H128a64 64 0 0 1-64-64V128z m64 0v352h141.248l41.984 41.984 108.16-216.32a32 32 0 0 1 59.52 5.888l72.32 265.344 91.52-205.888A32 32 0 0 1 697.6 364.8l86.4 115.2H896V128H128z m0 416V896h768V544h-128a32 32 0 0 1-25.6-12.8l-63.296-84.416-105.856 238.208a32 32 0 0 1-60.16-4.544L439.232 409.216l-90.56 181.12a32 32 0 0 1-51.2 8.32L242.688 544H128z">
        </path>
    </symbol>
    <symbol id="iconzoom-in" viewBox="0 0 1024 1024">
        <path
                d="M863.104 414.4c0 18.56-13.952 33.6-31.104 33.6H608.064a30.272 30.272 0 0 1-24.576-12.672A33.792 33.792 0 0 1 576 413.696V192c0-18.56 13.952-33.6 31.104-33.6 17.088 0 31.04 15.04 31.04 33.6v147.328l205.952-202.56c12.736-12.48 32.256-11.52 43.776 2.24a35.392 35.392 0 0 1-2.048 47.36L688 380.8H832c17.152 0 31.104 15.104 31.104 33.6zM448 192c0-17.152-15.104-31.104-33.6-31.104-18.56 0-33.6 13.952-33.6 31.104v144.064L186.368 138.24a35.392 35.392 0 0 0-47.36-2.048 29.376 29.376 0 0 0-2.24 43.776L339.328 385.92H192c-18.56 0-33.6 13.952-33.6 31.04 0 17.152 15.104 31.104 33.6 31.104h221.696a33.792 33.792 0 0 0 23.296-8.832A30.272 30.272 0 0 0 448 416V192zM192 576c-17.152 0-31.104 15.104-31.104 33.6 0 18.56 13.952 33.6 31.104 33.6h144.064L138.24 837.632a35.392 35.392 0 0 0-2.048 47.36c11.52 13.824 31.04 14.72 43.776 2.24l205.952-202.56V832c0 18.56 13.952 33.6 31.04 33.6 17.152 0 31.104-15.104 31.104-33.6V610.304a33.792 33.792 0 0 0-8.832-23.296A30.272 30.272 0 0 0 416 576H192zM576 832c0 17.152 15.104 31.104 33.6 31.104 18.56 0 33.6-13.952 33.6-31.104v-144.064l194.432 197.888c12.48 12.8 33.6 13.632 47.36 2.048a29.376 29.376 0 0 0 2.24-43.776L684.672 638.08H832c18.56 0 33.6-13.952 33.6-31.04 0-17.152-15.104-31.104-33.6-31.104H610.304a33.792 33.792 0 0 0-23.296 8.832A30.272 30.272 0 0 0 576 608V832z">
        </path>
    </symbol>
    <symbol id="iconnav-memberManagement" viewBox="0 0 1024 1024">
        <path
                d="M522.24 471.552A256 256 0 1 0 243.2 469.888a349.504 349.504 0 0 0-137.408 83.968C37.504 622.208 0 718.72 0 832v32h416v-64H65.152c6.272-84.096 37.312-152.32 85.952-200.96C205.696 544.512 285.12 512 384 512c60.352 0 117.056 15.168 166.592 41.856l30.336-56.32a413.952 413.952 0 0 0-58.688-25.984zM384 448a192 192 0 1 1 0-384 192 192 0 0 1 0 384z">
        </path>
        <path
                d="M920.96 566.592a32 32 0 0 1 51.456 8.832 224.064 224.064 0 0 1-277.376 307.584l-83.2 83.2a96 96 0 1 1-135.68-135.808l83.072-83.2a224 224 0 0 1 307.648-277.376 32 32 0 0 1 8.832 51.52l-82.816 82.752v45.248h45.248l82.816-82.752z m-128-52.992a160.064 160.064 0 0 0-167.04 227.392l9.664 20.416-114.24 114.24a32 32 0 1 0 45.312 45.248l114.24-114.24 20.416 9.792a160.064 160.064 0 0 0 227.392-167.04l-45.312 45.248c-37.504 37.44-135.744 0-135.744 0s-37.504-98.304 0-135.808l45.248-45.248z">
        </path>
    </symbol>
    <symbol id="iconnav-taskCenter" viewBox="0 0 1024 1024">
        <path
                d="M319.36 593.152l45.248-45.248 113.088 113.088 203.648-203.648 45.312 45.312-248.96 248.896L319.36 593.152z">
        </path>
        <path
                d="M345.664 160a191.936 191.936 0 0 1 332.672 0H832A96 96 0 0 1 928 256v640a96 96 0 0 1-96 96H192A96 96 0 0 1 96 896V256A96 96 0 0 1 192 160h153.664zM512 128a128 128 0 0 0-128 128h256a128 128 0 0 0-128-128z m189.376 96c1.728 10.432 2.624 21.12 2.624 32v58.048a5.952 5.952 0 0 1-5.952 5.952H325.952A5.952 5.952 0 0 1 320 314.048V256c0-10.88 0.896-21.568 2.624-32H192a32 32 0 0 0-32 32v640a32 32 0 0 0 32 32h640a32 32 0 0 0 32-32V256a32 32 0 0 0-32-32h-130.624z">
        </path>
    </symbol>
    <symbol id="iconnav-alertMessages" viewBox="0 0 1024 1024">
        <path
                d="M448 64a64 64 0 0 1 128 0v38.016a340.16 340.16 0 0 1 276.032 334.016v166.656L947.2 729.6A64 64 0 0 1 896 832h-224a160 160 0 0 1-320 0H128a64 64 0 0 1-51.2-102.4l95.168-126.912V436.032A340.16 340.16 0 0 1 448 102.016V64z m-32 768a96 96 0 0 0 192 0h-192zM235.968 436.032V624L128 768h768l-107.968-144V436.032a276.032 276.032 0 0 0-552.064 0z">
        </path>
    </symbol>
    <symbol id="iconfilter" viewBox="0 0 1024 1024">
        <path
                d="M184.512 59.776C106.496 59.776 61.056 147.84 106.368 211.456L322.56 515.072v336.96h64V494.592L158.464 174.336a32 32 0 0 1 26.048-50.56h479.552a32 32 0 0 1 25.792 50.88L455.296 494.336v457.92h64V515.328l222.144-302.72c46.528-63.36 1.28-152.768-77.44-152.768H184.576z">
        </path>
        <path
                d="M972.864 610.112H607.872v-64h365.056v64zM607.872 759.808h255.552v-64H607.872v64zM607.872 909.44h182.528v-64H607.872v64z">
        </path>
    </symbol>
    <symbol id="iconrefresh" viewBox="0 0 1024 1024">
        <path
                d="M480 96a384 384 0 1 0 271.36 655.744l45.184 45.312a448 448 0 1 1 120.768-414.72l47.232-78.784 54.912 32.896-112.512 187.456-187.392-112.448 32.896-54.912 107.008 64.192A384.064 384.064 0 0 0 480 96z">
        </path>
    </symbol>
    <symbol id="icontime" viewBox="0 0 1024 1024">
        <path d="M480 195.776v359.04l197.888 141.312 37.184-52.032L544 521.856V195.84h-64z"></path>
        <path d="M960 512A448 448 0 1 1 64 512a448 448 0 0 1 896 0z m-64 0A384 384 0 1 0 128 512a384 384 0 0 0 768 0z">
        </path>
    </symbol>
    <symbol id="iconoverview-ignore" viewBox="0 0 1024 1024">
        <path
                d="M86.656 41.344a32 32 0 0 0-45.248 45.248l896 896a32 32 0 0 0 45.248-45.248l-896-896zM456.256 119.68a55.744 55.744 0 1 1 111.488 0v33.152a296.256 296.256 0 0 1 240.384 290.944v145.152l82.88 110.528c14.272 19.008 14.144 42.176 4.352 60.16l-151.232-151.232V443.776a232.128 232.128 0 0 0-359.04-194.432l-46.016-46.08a294.656 294.656 0 0 1 117.184-50.432v-33.088zM588.864 724.608l64 64h-1.536a139.328 139.328 0 0 1-278.656 0H177.536a55.744 55.744 0 0 1-44.608-89.152l82.944-110.528V443.776c0-28.096 3.84-55.232 11.2-80.96l54.272 54.272a234.624 234.624 0 0 0-1.472 26.688v166.464l-85.76 114.368H588.8z m-160.448 64a83.584 83.584 0 0 0 167.168 0H428.416z">
        </path>
    </symbol>
    <symbol id="iconnav-recipientSettings" viewBox="0 0 1024 1024">
        <path
                d="M907.008 335.68a192 192 0 0 0-102.976-251.264l24.704-59.008a256 256 0 0 1 137.344 334.976l-59.072-24.704zM584.128 600.832a256 256 0 1 0-272.32 0A384.576 384.576 0 0 0 64 960h768a384.576 384.576 0 0 0-247.872-359.168zM640 384a192 192 0 1 1-384 0 192 192 0 0 1 384 0z m-192 256a320.128 320.128 0 0 1 313.6 256H134.4A320.128 320.128 0 0 1 448 640zM725.12 190.144a96 96 0 0 1 51.456 125.632l59.072 24.704a160 160 0 0 0-85.824-209.344l-24.704 59.008z">
        </path>
    </symbol>
    <symbol id="iconsettings-othersettings" viewBox="0 0 1024 1024">
        <path d="M256 256h64v64H256V256zM448 256H384v64h64V256zM256 512h512v64H256V512zM768 640H256v64h512v-64z"></path>
        <path
                d="M128 192a64 64 0 0 1 64-64h640a64 64 0 0 1 64 64v640a64 64 0 0 1-64 64H192a64 64 0 0 1-64-64V192z m64 192h640V192H192v192z m0 64v384h640V448H192z">
        </path>
    </symbol>
    <symbol id="iconalert-edit" viewBox="0 0 1024 1024">
        <path
                d="M360.96 493.76a64 64 0 0 0-18.688 43.904l-1.664 80.32a64 64 0 0 0 64.832 65.28l81.28-1.024a64 64 0 0 0 44.48-18.752l343.04-343.04a64 64 0 0 0 0-90.56L794.88 150.464a64 64 0 0 0-90.496 0L360.96 493.76z m468.032-218.624l-41.152 41.152-79.744-79.104 41.472-41.472 79.424 79.424z m-86.4 86.4L485.952 618.24l-81.344 1.024 1.664-80.256 256.576-256.64 79.744 79.168z">
        </path>
        <path
                d="M160 234.688a32 32 0 0 1 32-32h254.72a32 32 0 1 0 0-64H192a96 96 0 0 0-96 96V832A96 96 0 0 0 192 928h597.312a96 96 0 0 0 96-96V572.416a32 32 0 0 0-64 0V832a32 32 0 0 1-32 32H192a32 32 0 0 1-32-32V234.688z">
        </path>
    </symbol>
    <symbol id="iconnav-alertRules" viewBox="0 0 1024 1024">
        <path
                d="M512 0a64 64 0 0 0-64 64v38.016a340.16 340.16 0 0 0-276.032 334.016v166.656L76.8 729.6A64 64 0 0 0 128 832h320v-64H128l107.968-144V436.032a276.032 276.032 0 1 1 552.064 0V448h64v-11.968A340.16 340.16 0 0 0 576 102.016V64a64 64 0 0 0-64-64zM832 768a64 64 0 1 1-128 0 64 64 0 0 1 128 0z">
        </path>
        <path
                d="M824.704 555.136c-16.768-57.536-96.64-57.536-113.408 0a58.88 58.88 0 0 1-78.912 38.72c-54.528-22.464-104.32 41.216-70.72 90.432a60.992 60.992 0 0 1-19.52 87.04c-51.2 29.44-33.472 108.8 25.28 112.768 34.816 2.304 60.16 34.624 54.592 69.76-9.344 59.264 62.592 94.592 102.208 50.176a58.432 58.432 0 0 1 87.552 0c39.616 44.416 111.552 9.088 102.208-50.176a60.096 60.096 0 0 1 54.592-69.76c58.752-3.904 76.48-83.328 25.28-112.768a60.992 60.992 0 0 1-19.52-87.04c33.664-49.28-16.128-112.896-70.72-90.432a58.88 58.88 0 0 1-78.912-38.72z m-195.52 110.08A126.784 126.784 0 0 0 768 597.056a126.784 126.784 0 0 0 138.88 68.16 131.712 131.712 0 0 0 34.24 153.152 129.536 129.536 0 0 0-96.064 122.816c-45.376-35.2-108.8-35.2-154.112 0a129.536 129.536 0 0 0-96-122.88 131.712 131.712 0 0 0 34.24-153.088z">
        </path>
    </symbol>
    <symbol id="iconsettings-notificationEndpoint" viewBox="0 0 1024 1024">
        <path
                d="M640 224a32 32 0 0 1 32-32C834.496 192 960 338.944 960 512s-125.504 320-288 320a32 32 0 0 1 0-64c120.32 0 224-110.976 224-256s-103.68-256-224-256a32 32 0 0 1-32-32zM256 352l153.6-115.2a64 64 0 0 1 102.4 51.2v448a64 64 0 0 1-102.4 51.2L256 672H128a64 64 0 0 1-64-64v-192a64 64 0 0 1 64-64h128z m192 384v-448L320 384v256l128 96z m-192-128v-192H128v192h128z">
        </path>
        <path
                d="M608 320a32 32 0 0 0 0 64c25.472 0 57.984 13.44 84.736 38.336 26.368 24.512 43.264 56.512 43.264 89.664s-16.896 65.152-43.264 89.664C665.984 626.56 633.472 640 608 640a32 32 0 0 0 0 64c45.248 0 92.672-22.4 128.32-55.424 35.84-33.408 63.68-81.344 63.68-136.576s-27.776-103.168-63.68-136.576C700.608 342.336 653.248 320 608 320z">
        </path>
    </symbol>
    <symbol id="iconsettings-systemSettings" viewBox="0 0 1024 1024">
        <path d="M512 480a32 32 0 1 0 0-64 32 32 0 0 0 0 64z"></path>
        <path
                d="M437.696 293.696l-46.272 21.12a64 64 0 0 0-36.096 45.248l-10.24 49.856-12.416 49.28a64 64 0 0 0 12.928 56.512l32.512 39.04 30.848 40.448a64 64 0 0 0 52.224 25.088L512 619.264l50.816 1.024A64 64 0 0 0 615.04 595.2l30.848-40.448 32.512-39.04a64 64 0 0 0 12.928-56.512l-12.352-49.28-10.24-49.856a64 64 0 0 0-36.16-45.248l-46.272-21.12-45.376-23.04a64 64 0 0 0-57.856 0l-45.44 23.04z m120.832 57.664l47.488 21.696 10.56 51.072 12.672 50.624-33.408 40.128-31.68 41.472L512 555.264l-52.16 1.088-31.68-41.472-33.408-40.128 12.672-50.56 10.56-51.2 47.488-21.632L512 327.744l46.528 23.68z">
        </path>
        <path
                d="M640 768h256a64 64 0 0 0 64-64V192a64 64 0 0 0-64-64H128a64 64 0 0 0-64 64v512a64 64 0 0 0 64 64h256v64H224a32 32 0 0 0 0 64h576a32 32 0 0 0 0-64H640v-64z m256-576v512H128V192h768z m-448 576h128v64H448v-64z">
        </path>
    </symbol>
    <symbol id="iconnav-notification" viewBox="0 0 1024 1024">
        <path d="M960 224a160 160 0 1 1-320 0 160 160 0 0 1 320 0z m-64 0a96 96 0 1 0-192 0 96 96 0 0 0 192 0z"></path>
        <path
                d="M160 256A96 96 0 0 1 256 160h256v-64H256A160 160 0 0 0 96 256v512A160 160 0 0 0 256 928h512a160 160 0 0 0 160-160V512h-64v256a96 96 0 0 1-96 96H256A96 96 0 0 1 160 768V256z">
        </path>
        <path d="M288 448H640V384H288v64zM768 672H288v-64H768v64z"></path>
    </symbol>
    <symbol id="iconalert-delete" viewBox="0 0 1024 1024">
        <path
                d="M341.312 128a64 64 0 0 1 64-64h213.376a64 64 0 0 1 64 64v53.312h170.624a32 32 0 1 1 0 64H170.688a32 32 0 1 1 0-64h170.624V128z m64 0v42.688h213.376V128H405.312zM448 512a32 32 0 0 0-64 0v256a32 32 0 0 0 64 0V512zM608 480a32 32 0 0 1 32 32v256a32 32 0 0 1-64 0V512a32 32 0 0 1 32-32z">
        </path>
        <path
                d="M240.64 298.688a64 64 0 0 0-63.808 69.632l45.568 512a64 64 0 0 0 63.744 58.368h451.712a64 64 0 0 0 63.744-58.368l45.568-512a64 64 0 0 0-63.744-69.632H240.576z m542.72 64l-45.44 512H286.08l-45.44-512h542.72z">
        </path>
    </symbol>
    <symbol id="iconoverview-alert" viewBox="0 0 1024 1024">
        <path
                d="M554.24 104.896a56.64 56.64 0 0 1 69.184-39.04 55.04 55.04 0 0 1 39.936 67.584l-8.64 31.616c127.168 59.968 196.928 201.408 159.04 339.456l-38.08 138.624 52.16 126.848c17.28 42.176-22.08 85.504-67.072 73.792l-190.976-49.92c-20.224 73.472-97.664 117.12-172.992 97.408-75.392-19.712-120.064-95.296-99.84-168.768l-191.04-49.92a54.656 54.656 0 0 1-20.288-96.64L195.84 491.52l38.08-138.624c37.952-138.048 170.816-226.56 311.68-216.384l8.704-31.616z m-202.752 631.872c-12.16 44.096 14.72 89.408 59.904 101.248 45.248 11.84 91.712-14.336 103.808-58.496l-163.712-42.752z">
        </path>
    </symbol>
    <symbol id="iconnav-Capacity" viewBox="0 0 1024 1024">
        <path
                d="M496.832 67.84a32 32 0 0 1 30.336 0l416 224A32 32 0 0 1 960 320v384a32 32 0 0 1-16.832 28.16l-416 224a32 32 0 0 1-30.336 0l-416-224A32 32 0 0 1 64 704V320a32 32 0 0 1 16.832-28.16l416-224zM128 373.568v311.296l352 189.568V563.136L128 373.568z m416 500.864L896 684.8V373.568L544 563.2v311.296zM512 507.712L860.48 320 512 132.352 163.52 320 512 507.648z">
        </path>
    </symbol>
    <symbol id="iconclose" viewBox="0 0 1024 1024">
        <path
                d="M867.584 240.448l-90.496-90.496L505.6 421.504 234.048 149.952 143.552 240.448 415.04 512l-271.488 271.552 90.496 90.496 271.488-271.552 271.552 271.552 90.496-90.496L596.096 512l271.488-271.552z">
        </path>
    </symbol>
    <symbol id="iconWebLink" viewBox="0 0 1024 1024">
        <path
                d="M288 352a64 64 0 0 1 0-128h448a64 64 0 0 1 64 64v448a64 64 0 1 1-128 0V442.496l-338.752 338.752a64 64 0 0 1-90.496-90.496L581.504 352H288z">
        </path>
    </symbol>
    <symbol id="iconDownload" viewBox="0 0 1024 1024">
        <path
                d="M544 96a32 32 0 0 0-64 0v469.632l-105.984-100.48a32 32 0 0 0-44.032 46.528l160 151.552a32 32 0 0 0 44.032 0l160-151.552a32 32 0 0 0-44.032-46.464L544 565.632V96z">
        </path>
        <path
                d="M128 608a32 32 0 0 0-32 32v256a32 32 0 0 0 32 32h768a32 32 0 0 0 32-32v-256a32 32 0 0 0-32-32h-96a32 32 0 0 0 0 64h64v192h-704v-192h64a32 32 0 0 0 0-64H128z">
        </path>
    </symbol>
    <symbol id="iconnav-book" viewBox="0 0 1024 1024">
        <path
                d="M832 64a64 64 0 0 1 64 64v768a64 64 0 0 1-64 64H256a128 128 0 0 1-128-128V192a128 128 0 0 1 128-128h576zM192 832a64 64 0 0 0 64 64h576v-128H256c-8.512 0-21.632 4.544-36.224 11.2A63.936 63.936 0 0 0 192 832zM832 128h-64v256a32 32 0 0 1-51.2 25.6l-57.6-43.2a32 32 0 0 0-38.4 0l-57.6 43.2A32 32 0 0 1 512 384V128H256a64 64 0 0 0-64 64v529.152A127.36 127.36 0 0 1 256 704h576V128z m-128 0H576v192l6.4-4.8a96 96 0 0 1 115.2 0L704 320V128z">
        </path>
    </symbol>
    <symbol id="iconnav-operationLog" viewBox="0 0 1024 1024">
        <path
                d="M96 192A96 96 0 0 1 192 96h576A96 96 0 0 1 864 192v192h-64V192a32 32 0 0 0-32-32H192a32 32 0 0 0-32 32v640a32 32 0 0 0 32 32h256v64H192A96 96 0 0 1 96 832V192z">
        </path>
        <path
                d="M704 352H256v-64h448v64zM256 544h192v-64H256v64zM384 736H256v-64h128v64zM601.28 495.68a32 32 0 0 0-8.832 51.52l82.752 82.752v45.248h-45.248L547.2 592.448a32 32 0 0 0-51.52 8.832A224.064 224.064 0 0 0 773.12 908.8l83.136 83.136a96 96 0 1 0 135.744-135.744l-83.136-83.2a224 224 0 0 0-307.584-277.376z m-16.576 315.328a159.808 159.808 0 0 1-45.248-135.808l45.248 45.248c37.504 37.504 135.744 0 135.744 0s37.504-98.24 0-135.744l-45.248-45.248a160.064 160.064 0 0 1 167.04 227.392l-9.728 20.416 114.24 114.24a32 32 0 1 1-45.248 45.248L787.2 832.512l-20.48 9.728c-59.52 28.48-132.864 17.92-182.08-31.232z">
        </path>
    </symbol>
    <symbol id="iconnav-systemSetting" viewBox="0 0 1024 1024">
        <path d="M704 512a192 192 0 1 1-384 0 192 192 0 0 1 384 0z m-64 0a128 128 0 1 0-256 0 128 128 0 0 0 256 0z">
        </path>
        <path
                d="M662.08 124.032a128 128 0 0 1 110.848 64L923.072 448a128 128 0 0 1 0 128l-150.144 259.968a128 128 0 0 1-110.848 64h-300.16a128 128 0 0 1-110.848-64L100.928 576a128 128 0 0 1 0-128l150.144-259.968a128 128 0 0 1 110.848-64h300.16z m55.424 96a64 64 0 0 0-55.424-32h-300.16a64 64 0 0 0-55.424 32L156.352 480a64 64 0 0 0 0 64l150.144 259.968a64 64 0 0 0 55.424 32h300.16a64 64 0 0 0 55.424-32L867.648 544a64 64 0 0 0 0-64L717.44 220.032z">
        </path>
    </symbol>
    <symbol id="iconnav-setting" viewBox="0 0 1024 1024">
        <path
                d="M443.968 288a128 128 0 0 1-247.936 0H128v-64h68.032a128 128 0 0 1 247.936 0H896v64H443.968zM384 256a64 64 0 1 0-128 0 64 64 0 0 0 128 0zM580.032 480a128 128 0 0 1 247.936 0H896v64h-68.032a128 128 0 0 1-247.936 0H128v-64h452.032zM768 512a64 64 0 1 0-128 0 64 64 0 0 0 128 0zM320 640a128 128 0 0 0-123.968 96H128v64h68.032a128 128 0 0 0 247.936 0H896v-64H443.968A128 128 0 0 0 320 640z m0 192a64 64 0 1 1 0-128 64 64 0 0 1 0 128z">
        </path>
    </symbol>
    <symbol id="iconswitch" viewBox="0 0 1024 1024">
        <path
                d="M626.048 186.048l117.76 181.952H128v96h792.256L706.56 133.952l-80.576 52.096zM397.952 837.952l-117.76-181.952H896v-96H103.744l213.632 330.048 80.576-52.096z">
        </path>
    </symbol>
    <symbol id="iconnav-versionManagement" viewBox="0 0 1024 1024">
        <path
                d="M800 64A96 96 0 0 1 896 160v512a96 96 0 0 1-96 96h-128v96A96 96 0 0 1 576 960H192a96 96 0 0 1-96-96v-512A96 96 0 0 1 192 256h128V160A96 96 0 0 1 416 64h384zM384 256h192a96 96 0 0 1 96 96V704h128a32 32 0 0 0 32-32v-512a32 32 0 0 0-32-32h-384a32 32 0 0 0-32 32V256zM160 352v512a32 32 0 0 0 32 32h384a32 32 0 0 0 32-32v-512A32 32 0 0 0 576 320H192a32 32 0 0 0-32 32z">
        </path>
    </symbol>
    <symbol id="iconnav-multiCluster" viewBox="0 0 1024 1024">
        <path
                d="M128 64a64 64 0 0 0-64 64v256a64 64 0 0 0 64 64h96v128H128a64 64 0 0 0-64 64v256a64 64 0 0 0 64 64h256a64 64 0 0 0 64-64v-96h128V896a64 64 0 0 0 64 64h256a64 64 0 0 0 64-64v-256a64 64 0 0 0-64-64h-96V448H896a64 64 0 0 0 64-64V128a64 64 0 0 0-64-64h-256a64 64 0 0 0-64 64v96H448V128a64 64 0 0 0-64-64H128z m256 64v256H128V128h256z m192 160V384a64 64 0 0 0 64 64h96v128H640a64 64 0 0 0-64 64v96H448V640a64 64 0 0 0-64-64H288V448H384a64 64 0 0 0 64-64V288h128z m320 352v256h-256v-256h256zM128 640h256v256H128v-256z m512-512h256v256h-256V128z">
        </path>
    </symbol>
    <symbol id="iconnav-partitionInfo" viewBox="0 0 1024 1024">
        <path
                d="M903.288471 527.661176A421.707294 421.707294 0 0 0 60.235294 542.117647a421.647059 421.647059 0 1 0 843.053177-14.456471z m-449.957647-345.871058V571.632941h388.758588a361.411765 361.411765 0 1 1-388.758588-389.842823z m60.235294 329.607529V179.019294l35.538823 7.951059a361.833412 361.833412 0 0 1 292.924235 324.367059l-328.463058 0.060235z">
        </path>
    </symbol>
    <symbol id="iconnav-graph" viewBox="0 0 1024 1024">
        <path
                d="M256 128.064a128 128 0 0 1 110.272 193.088l80.896 80.896a127.36 127.36 0 0 1 64.96-17.728c23.68 0 45.824 6.4 64.832 17.664l80.896-80.896a128 128 0 1 1 45.248 45.248L622.272 447.168a127.36 127.36 0 0 1 0.128 130.24l80.704 80.64a128 128 0 1 1-45.248 45.248L577.152 622.72a127.36 127.36 0 0 1-65.024 17.728 127.36 127.36 0 0 1-65.152-17.856L366.272 703.36a128 128 0 1 1-45.248-45.248L401.664 577.28a127.36 127.36 0 0 1-17.6-64.896c0-23.68 6.464-45.952 17.728-65.024L320.96 366.4A128 128 0 1 1 256 128.128z m0 64a64 64 0 1 0 0 128 64 64 0 0 0 0-128z m512.128 0a64 64 0 1 0 0 128 64 64 0 0 0 0-128z m-64 576.32a64 64 0 1 0 128 0 64 64 0 0 0-128 0z m-256-256a64 64 0 1 0 128 0 64 64 0 0 0-128 0z m-192.128 192a64 64 0 1 0 0 128 64 64 0 0 0 0-128z">
        </path>
    </symbol>
    <symbol id="iconnav-partionDistributed" viewBox="0 0 1024 1024">
        <path
                d="M576 128a64 64 0 0 1 64-64h256a64 64 0 0 1 64 64v768a64 64 0 0 1-64 64h-256a64 64 0 0 1-64-64H384a64 64 0 0 1-64-64H128a64 64 0 0 1-64-64V256a64 64 0 0 1 64-64h192a64 64 0 0 1 64-64h192z m64 0v768h256V128h-256zM576 192H384v640h192V192zM320 256H128v512h192V256z">
        </path>
    </symbol>
    <symbol id="iconCustomerService" viewBox="0 0 1024 1024">
        <path
                d="M288 384a224 224 0 1 1 448 0v64h-29.44a2.56 2.56 0 0 0-2.56 2.56v314.88c0 1.408 1.152 2.56 2.56 2.56h29.44v32.192a32 32 0 0 1-32 32H576a32 32 0 0 0 0 64h128a96 96 0 0 0 96-96V768a160 160 0 0 0 0-320V384a288 288 0 1 0-576 0v64a160 160 0 0 0 0 320h93.44a2.56 2.56 0 0 0 2.56-2.56V450.56A2.56 2.56 0 0 0 317.44 448H288V384z m512 320H768V512h32a96 96 0 0 1 0 192zM256 512v192h-32a96 96 0 0 1 0-192H256z">
        </path>
    </symbol>
    <symbol id="icondate-right" viewBox="0 0 1024 1024">
        <path
                d="M406.656 790.656a32 32 0 0 1-48.96-40.832l3.648-4.48L594.752 512 361.344 278.656a32 32 0 0 1-3.648-40.832l3.648-4.48a32 32 0 0 1 40.832-3.648l4.48 3.648 256 256a32 32 0 0 1 3.648 40.832l-3.648 4.48-256 256z">
        </path>
    </symbol>
    <symbol id="icondate-left2" viewBox="0 0 1024 1024">
        <path
                d="M534.656 233.344a32 32 0 0 0-45.312 0l-256 256-3.648 4.48a32 32 0 0 0 3.648 40.832l256 256 4.48 3.648a32 32 0 0 0 40.832-3.648l3.648-4.48a32 32 0 0 0-3.648-40.832L301.248 512l233.408-233.344 3.648-4.48a32 32 0 0 0-3.648-40.832z">
        </path>
        <path
                d="M790.656 233.344a32 32 0 0 0-45.312 0l-256 256-3.648 4.48a32 32 0 0 0 3.648 40.832l256 256 4.48 3.648a32 32 0 0 0 40.832-3.648l3.648-4.48a32 32 0 0 0-3.648-40.832L557.248 512l233.408-233.344 3.648-4.48a32 32 0 0 0-3.648-40.832z">
        </path>
    </symbol>
    <symbol id="icondate-left" viewBox="0 0 1024 1024">
        <path
                d="M617.344 233.344a32 32 0 0 1 48.96 40.832l-3.648 4.48L429.248 512l233.408 233.344a32 32 0 0 1 3.648 40.832l-3.648 4.48a32 32 0 0 1-40.832 3.648l-4.48-3.648-256-256a32 32 0 0 1-3.648-40.832l3.648-4.48 256-256z">
        </path>
    </symbol>
    <symbol id="iconfiles" viewBox="0 0 1024 1024">
        <path
                d="M192 192a64 64 0 0 1 64-64h327.04a64 64 0 0 1 45.568 19.072l184.96 187.904a64 64 0 0 1 18.432 44.928V832a64 64 0 0 1-64 64H256a64 64 0 0 1-64-64V192z m352 0H256v640h512V416H608a64 64 0 0 1-64-64V192z m196.544 160L608 217.408V352h132.544z">
        </path>
    </symbol>
    <symbol id="icondate" viewBox="0 0 1024 1024">
        <path
                d="M448 576.32a32 32 0 0 0-32-32h-64l-5.76 0.512a32 32 0 0 0 5.76 63.488h64l5.76-0.512A32 32 0 0 0 448 576.32zM416 672.32a32 32 0 0 1 5.76 63.488l-5.76 0.512h-64a32 32 0 0 1-5.76-63.488l5.76-0.512h64zM704 576.32a32 32 0 0 0-32-32h-64l-5.76 0.512a32 32 0 0 0 5.76 63.488h64l5.76-0.512a32 32 0 0 0 26.24-31.488zM672 672.32a32 32 0 0 1 5.76 63.488l-5.76 0.512h-64a32 32 0 0 1-5.76-63.488l5.76-0.512h64z">
        </path>
        <path
                d="M383.488 138.24A32 32 0 0 0 320 144V192H192a64 64 0 0 0-64 64v576a64 64 0 0 0 64 64h640a64 64 0 0 0 64-64V256a64 64 0 0 0-64-64h-128v-48l-0.512-5.76a32 32 0 0 0-63.488 5.76V192H384v-48l-0.512-5.76zM832 384H192V256h128v48l0.512 5.76A32 32 0 0 0 384 304V256h256v48l0.512 5.76A32 32 0 0 0 704 304V256h128v128zM192 448h640v384H192V448z">
        </path>
    </symbol>
    <symbol id="iconabnormal" viewBox="0 0 1024 1024">
        <path
                d="M1024 512A512 512 0 1 1 0 512a512 512 0 0 1 1024 0zM568.896 398.208H455.04v398.208H568.96V398.208z m0-56.896V227.584H455.04v113.728H568.96z">
        </path>
    </symbol>
    <symbol id="iconhelp" viewBox="0 0 1024 1024">
        <path
                d="M512 224a160 160 0 0 1 40.96 314.752l-8.96 1.984v100.288h-64V480H512a96 96 0 1 0-95.552-105.216L416 384h-64A160 160 0 0 1 512 224zM544 768.512v-64.384h-64v64.384h64z">
        </path>
        <path d="M960 512A448 448 0 1 0 64 512a448 448 0 0 0 896 0zM128 512a384 384 0 1 1 768 0A384 384 0 0 1 128 512z">
        </path>
    </symbol>
    <symbol id="iconlock" viewBox="0 0 1024 1024">
        <path d="M544.256 631.296v40.704a32 32 0 0 1-64 0v-40.384A64 64 0 0 1 512 512h0.96a64 64 0 0 1 31.36 119.296z">
        </path>
        <path
                d="M288 384a224 224 0 1 1 448 0h32a64 64 0 0 1 64 64v320a64 64 0 0 1-64 64H256a64 64 0 0 1-64-64V448a64 64 0 0 1 64-64h32z m64 0h320a160 160 0 0 0-320 0zM256 448v320h512V448H256z">
        </path>
    </symbol>
    <symbol id="iconmessage-alert" viewBox="0 0 1024 1024">
        <path
                d="M480.576 160a32 32 0 0 1 64 0v34.304A230.4 230.4 0 0 1 742.4 422.4v115.2c0 61.632 16.192 122.688 31.36 166.4h26.24a32 32 0 0 1 0 64H576a64 64 0 1 1-128 0H224a32 32 0 0 1 0-64h26.24c15.168-43.712 31.36-104.768 31.36-166.4V422.4a230.4 230.4 0 0 1 198.976-228.288V160z m225.792 544c-14.208-45.12-27.968-104.576-27.968-166.4V422.4a166.4 166.4 0 1 0-332.8 0v115.2c0 61.824-13.824 121.28-27.968 166.4h388.736z">
        </path>
    </symbol>
    <symbol id="icondown" viewBox="0 0 1024 1024">
        <path
                d="M512 960.768l448-440.448-72.768-71.552-320.64 315.328V64H457.408v700.16L136.768 448.704 64 520.32l448 440.448z">
        </path>
    </symbol>
    <symbol id="iconCorrect" viewBox="0 0 1024 1024">
        <path
                d="M372.032 727.872a32 32 0 0 0 41.6-3.072l407.296-407.296a32 32 0 0 0-45.248-45.248L391.296 656.64 232.448 497.792a32 32 0 0 0-45.248 45.248l178.112 178.176a32.448 32.448 0 0 0 6.656 6.656z">
        </path>
    </symbol>
    <symbol id="iconmessage" viewBox="0 0 1024 1024">
        <path
                d="M128 256a64 64 0 0 1 64-64h640a64 64 0 0 1 64 64v512a64 64 0 0 1-64 64H192a64 64 0 0 1-64-64V256z m704 64L602.496 549.504a128 128 0 0 1-180.992 0L192 320v448h640V320zM218.496 256l248.256 248.256a64 64 0 0 0 90.496 0L805.504 256H218.496z">
        </path>
    </symbol>
    <symbol id="iconnav-accountList" viewBox="0 0 1024 1024">
        <path
                d="M576 352c0 78.08-40 146.88-100.608 186.944C592 592.512 672 719.616 672 864h-64c0-162.304-117.76-288-256-288s-256 125.696-256 288h-64c0-144.384 80-271.488 196.608-325.056A224 224 0 1 1 576 352zM352 512a160 160 0 1 0 0-320 160 160 0 0 0 0 320zM672 320H1024V256h-352v64zM1024 544h-320v-64h320v64zM768 768h256v-64h-256v64z">
        </path>
    </symbol>
    <symbol id="iconnav-accountManagement" viewBox="0 0 1024 1024">
        <path
                d="M960 512A448 448 0 1 1 64 512a448 448 0 0 1 896 0zM230.08 772.736a288.512 288.512 0 0 1 165.312-204.16 192 192 0 1 1 233.152 0 288.512 288.512 0 0 1 165.376 204.16 384 384 0 1 0-563.84 0z m505.792 51.328a224 224 0 0 0-447.744 0A382.272 382.272 0 0 0 512 896a382.272 382.272 0 0 0 223.872-71.936zM640 416a128 128 0 1 0-256 0 128 128 0 0 0 256 0z">
        </path>
    </symbol>
    <symbol id="iconnav-Clusters" viewBox="0 0 1024 1024">
        <path
                d="M64 576a64 64 0 0 0 64 64h128a64 64 0 0 0 64-64v-32h96V768A96 96 0 0 0 512 864h64v32a64 64 0 0 0 64 64h256a64 64 0 0 0 64-64v-128a64 64 0 0 0-64-64h-256a64 64 0 0 0-64 64v32H512a32 32 0 0 1-32-32V544H576V576a64 64 0 0 0 64 64h256a64 64 0 0 0 64-64V448a64 64 0 0 0-64-64h-256a64 64 0 0 0-64 64v32H480V256a32 32 0 0 1 32-32h64V256a64 64 0 0 0 64 64h256a64 64 0 0 0 64-64V128a64 64 0 0 0-64-64h-256a64 64 0 0 0-64 64v32H512A96 96 0 0 0 416 256v224H320V448a64 64 0 0 0-64-64H128a64 64 0 0 0-64 64v128z m64-128h128v128H128V448z m512 448v-128h256v128h-256z m0-320V448h256v128h-256z m0-320V128h256v128h-256z">
        </path>
    </symbol>
    <symbol id="iconnav-foldTriangle" viewBox="0 0 1024 1024">
        <path
                d="M238.08 429.696c-40.32-40.32-11.712-109.248 45.312-109.248h459.904c56.96 0 85.568 68.928 45.248 109.248l-229.952 229.952a64 64 0 0 1-90.496 0L238.08 429.696z">
        </path>
    </symbol>
    <symbol id="iconnav-language-line" viewBox="0 0 1024 1024">
        <path
                d="M512 64a448 448 0 1 1 0 896A448 448 0 0 1 512 64zM160.384 357.376A384.064 384.064 0 0 0 352 861.184v-104.128l87.424-127.488 232.576 153.6v78.016a385.344 385.344 0 0 0 197.568-208.832L751.872 583.68V403.584l115.072-38.4A385.408 385.408 0 0 0 715.52 186.24l-4.608 7.488-7.808 8.64-90.752 69.248v110.976l-123.456 59.136v135.36H317.44L160.448 380.16v-22.784zM886.4 426.24l-70.528 23.488v97.216l72.32 42.24a385.6 385.6 0 0 0-1.792-162.944zM512 896c33.152 0 65.28-4.224 96-12.096l-0.064-66.304-151.36-99.968-40.64 59.264v107.008c30.72 7.872 62.912 12.096 96.064 12.096zM224.448 257.536L224.384 357.76l123.84 155.264h76.608V401.472l123.456-59.2V240l109.248-83.456A382.848 382.848 0 0 0 512 128a383.04 383.04 0 0 0-287.552 129.536z">
        </path>
    </symbol>
    <symbol id="icondate-right2" viewBox="0 0 1024 1024">
        <path
                d="M233.344 790.656a32 32 0 0 0 45.312 0l256-256 3.648-4.48a32 32 0 0 0-3.648-40.832l-256-256-4.48-3.648a32 32 0 0 0-40.832 3.648l-3.648 4.48a32 32 0 0 0 3.648 40.832L466.752 512l-233.408 233.344-3.648 4.48a32 32 0 0 0 3.648 40.832z">
        </path>
        <path
                d="M489.344 790.656a32 32 0 0 0 45.312 0l256-256 3.648-4.48a32 32 0 0 0-3.648-40.832l-256-256-4.48-3.648a32 32 0 0 0-40.832 3.648l-3.648 4.48a32 32 0 0 0 3.648 40.832L722.752 512l-233.408 233.344-3.648 4.48a32 32 0 0 0 3.648 40.832z">
        </path>
    </symbol>
    <symbol id="iconnav-cpu" viewBox="0 0 1024 1024">
        <path
                d="M576 384a64 64 0 0 1 64 64v128a64 64 0 0 1-64 64H448a64 64 0 0 1-64-64V448a64 64 0 0 1 64-64h128z m0 64H448v128h128V448z">
        </path>
        <path
                d="M640 256a128 128 0 0 1 128 128h96a32 32 0 0 1 0 64H768v128h96a32 32 0 0 1 0 64H768a128 128 0 0 1-128 128v96a32 32 0 0 1-64 0V768H448v96a32 32 0 0 1-64 0V768a128 128 0 0 1-128-128H160a32 32 0 0 1 0-64H256V448H160a32 32 0 0 1 0-64H256a128 128 0 0 1 128-128V160a32 32 0 0 1 64 0V256h128V160a32 32 0 0 1 64 0V256z m-256 448h256a64 64 0 0 0 63.552-56.512L704 640V384a64 64 0 0 0-56.512-63.552L640 320H384a64 64 0 0 0-63.552 56.512L320 384v256a64 64 0 0 0 56.512 63.552L384 704z">
        </path>
    </symbol>
    <symbol id="iconnav-configuration" viewBox="0 0 1024 1024">
        <path
                d="M481.581176 348.581647a193.234824 193.234824 0 1 0 0 386.529882 193.234824 193.234824 0 0 0 0-386.529882z m0 60.235294a132.999529 132.999529 0 1 1 0 266.059294 132.999529 132.999529 0 0 1 0-266.059294z">
        </path>
        <path
                d="M350.629647 121.795765c9.637647 1.807059 18.793412 5.601882 26.925177 11.143529l63.488 43.008a72.282353 72.282353 0 0 0 81.136941 0l63.427764-43.008a72.282353 72.282353 0 0 1 111.616 46.200471l14.456471 75.294117c5.541647 29.093647 28.310588 51.802353 57.344 57.404236l75.294118 14.45647a72.282353 72.282353 0 0 1 46.20047 111.49553l-43.068235 63.488a72.282353 72.282353 0 0 0 0 81.136941l43.068235 63.427765a72.282353 72.282353 0 0 1-46.20047 111.616l-75.294118 14.45647a72.282353 72.282353 0 0 0-57.404235 57.344l-14.456471 75.294118a72.282353 72.282353 0 0 1-111.555765 46.20047l-63.427764-43.068235a72.282353 72.282353 0 0 0-81.136941 0l-63.488 43.068235a72.282353 72.282353 0 0 1-111.555765-46.20047l-14.456471-75.294118a72.282353 72.282353 0 0 0-57.344-57.404235l-75.294117-14.456471a72.282353 72.282353 0 0 1-46.200471-111.495529l43.008-63.488a72.282353 72.282353 0 0 0 0-81.136941l-43.008-63.488a72.282353 72.282353 0 0 1 46.200471-111.555765l75.294117-14.456471a72.282353 72.282353 0 0 0 57.404236-57.344l14.45647-75.294117a72.282353 72.282353 0 0 1 84.570353-57.344z m285.515294 64.210823a12.047059 12.047059 0 0 0-13.251765-4.818823l-3.433411 1.626353-63.488 43.008a132.517647 132.517647 0 0 1-139.565177 5.662117L407.190588 225.882353l-63.488-43.008a12.047059 12.047059 0 0 0-17.52847 4.638118l-1.024 3.011764-14.456471 75.294118a132.517647 132.517647 0 0 1-95.352471 102.942118l-9.818352 2.288941-75.294118 14.45647a12.047059 12.047059 0 0 0-9.336471 15.058824l1.626353 3.493647 43.008 63.488a132.517647 132.517647 0 0 1 5.662118 139.565176l-5.662118 9.155765-43.008 63.488a12.047059 12.047059 0 0 0 4.638118 17.528471l3.011765 1.024 75.294117 14.45647a132.517647 132.517647 0 0 1 102.942118 95.352471l2.288941 9.818353 14.456471 75.294117a12.047059 12.047059 0 0 0 15.058823 9.336471l3.493647-1.626353 63.488-43.008a132.517647 132.517647 0 0 1 139.565177-5.722353l9.155764 5.722353 63.488 43.008a12.047059 12.047059 0 0 0 17.528471-4.638118l1.024-3.011764 14.456471-75.294118a132.517647 132.517647 0 0 1 95.35247-102.942118l9.818353-2.288941 75.294118-14.45647a12.047059 12.047059 0 0 0 9.33647-15.058824l-1.626353-3.493647-43.008-63.488a132.517647 132.517647 0 0 1-5.722353-139.565176l5.722353-9.155765 43.008-63.488a12.047059 12.047059 0 0 0-4.638117-17.528471l-3.011765-1.024-75.294118-14.45647a132.517647 132.517647 0 0 1-102.942117-95.352471l-2.288941-9.818353-14.456471-75.294117a12.167529 12.167529 0 0 0-1.807059-4.517647z">
        </path>
    </symbol>
    <symbol id="iconnav-serverdashboard" viewBox="0 0 1024 1024">
        <path
                d="M512 421.345882a30.117647 30.117647 0 0 1 29.635765 24.696471l0.481882 5.421176v180.705883a30.117647 30.117647 0 0 1-59.753412 5.421176l-0.481882-5.421176v-180.705883a30.117647 30.117647 0 0 1 30.117647-30.117647zM421.165176 566.573176a30.117647 30.117647 0 0 0-59.753411 5.421177v60.175059l0.481882 5.421176a30.117647 30.117647 0 0 0 59.753412-5.421176v-60.235294l-0.481883-5.421177zM271.058824 541.816471a30.117647 30.117647 0 0 1 29.635764 24.69647l0.481883 5.421177v60.235294a30.117647 30.117647 0 0 1-59.753412 5.421176l-0.481883-5.421176v-60.235294a30.117647 30.117647 0 0 1 30.117648-30.117647zM662.106353 385.807059a30.117647 30.117647 0 0 0-59.753412 5.421176v240.941177l0.481883 5.421176a30.117647 30.117647 0 0 0 59.753411-5.421176v-240.941177l-0.481882-5.421176zM752.941176 421.345882a30.117647 30.117647 0 0 1 29.635765 24.696471l0.481883 5.421176v180.705883a30.117647 30.117647 0 0 1-59.753412 5.421176l-0.481883-5.421176v-180.705883a30.117647 30.117647 0 0 1 30.117647-30.117647zM818.176 751.616a30.117647 30.117647 0 0 0-30.117647-30.117647H237.086118l-5.421177 0.481882a30.117647 30.117647 0 0 0 5.421177 59.753412h550.972235l5.421176-0.481882a30.117647 30.117647 0 0 0 24.696471-29.635765z">
        </path>
        <path
                d="M933.767529 240.941176a60.235294 60.235294 0 0 0-60.235294-60.235294H150.588235a60.235294 60.235294 0 0 0-60.235294 60.235294v602.352942a60.235294 60.235294 0 0 0 60.235294 60.235294h722.944a60.235294 60.235294 0 0 0 60.235294-60.235294V240.941176zM150.588235 240.941176h722.944v602.352942H150.588235V240.941176z">
        </path>
    </symbol>
    <symbol id="iconnav-load" viewBox="0 0 1024 1024">
        <path
                d="M512 128a128 128 0 0 1 89.28 219.712l132.992 232.768A128.128 128.128 0 0 1 896 704a128 128 0 0 1-251.968 32H379.968a128 128 0 1 1-90.24-155.52l132.992-232.768A128 128 0 0 1 512 128z m-33.728 251.52L345.28 612.288c16.576 16.064 28.8 36.672 34.688 59.712h264.064c5.952-23.04 18.176-43.648 34.688-59.712L545.728 379.52a128.128 128.128 0 0 1-67.456 0zM512 192a64 64 0 1 0 0 128 64 64 0 0 0 0-128z m-256 448a64 64 0 1 0 0 128 64 64 0 0 0 0-128z m448 64a64 64 0 1 0 128 0 64 64 0 0 0-128 0z">
        </path>
    </symbol>
    <symbol id="iconnav-disk" viewBox="0 0 1024 1024">
        <path d="M704 672a32 32 0 0 0-32-32h-320l-5.76 0.512A32 32 0 0 0 352 704h320l5.76-0.512A32 32 0 0 0 704 672z">
        </path>
        <path
                d="M678.976 192a96 96 0 0 1 86.272 54.016l3.648 8.384 113.856 304.64c5.76 9.728 9.92 20.608 12.032 32.192l2.048 5.568-1.216 0.448c0.448 3.584 0.64 7.232 0.64 10.944v128a96 96 0 0 1-96 96H223.808a96 96 0 0 1-96-96v-128c0-3.712 0.192-7.296 0.64-10.88l-1.28-0.512 2.112-5.568a95.36 95.36 0 0 1 12.032-32.192l113.792-304.64a96 96 0 0 1 80.768-61.952L345.024 192h333.952z m29.952 84.8a32 32 0 0 0-24.32-20.288L678.976 256H344.96a32 32 0 0 0-27.52 15.744l-2.432 5.12-87.936 235.328h569.792l-88-235.392zM198.656 588.352l-6.848 18.368v129.472a32 32 0 0 0 32 32h576.448a32 32 0 0 0 32-32v-128-1.472l-6.912-18.368a32 32 0 0 0-25.088-12.16H223.808a32 32 0 0 0-25.152 12.16z">
        </path>
    </symbol>
    <symbol id="iconnav-leaderDistributed" viewBox="0 0 1024 1024">
        <path
                d="M128 128a64 64 0 0 0-64 64v640a64 64 0 0 0 64 64h128a64 64 0 0 0 64-64V192a64 64 0 0 0-64-64H128z m128 64v640H128V192h128zM448 128a64 64 0 0 0-64 64v640a64 64 0 0 0 64 64h128a64 64 0 0 0 64-64V192a64 64 0 0 0-64-64H448z m128 64v640H448V192h128zM704 192a64 64 0 0 1 64-64h128a64 64 0 0 1 64 64v640a64 64 0 0 1-64 64h-128a64 64 0 0 1-64-64V192z m64 0v640h128V192h-128z">
        </path>
    </symbol>
    <symbol id="iconnav-meta" viewBox="0 0 1024 1024">
        <path
                d="M704.064 512a191.936 191.936 0 1 1-383.872 0 191.936 191.936 0 0 1 383.872 0zM640 512a127.872 127.872 0 1 0-255.68 0A127.872 127.872 0 0 0 640 512z">
        </path>
        <path
                d="M551.488 91.648l305.472 176.384c24.32 14.08 39.296 40 39.296 68.096v352.768c0 28.096-14.976 54.08-39.296 68.16l-305.472 176.32c-24.32 14.08-54.4 14.08-78.72 0l-305.408-176.32A78.72 78.72 0 0 1 128 688.896V336.128c0-28.096 14.976-54.08 39.36-68.096l305.408-176.384c24.32-14.08 54.4-14.08 78.72 0z m281.6 235.584L512.064 141.952 191.232 327.232v370.56l320.896 185.28 320.896-185.28v-370.56z">
        </path>
    </symbol>
    <symbol id="iconnav-Node" viewBox="0 0 1024 1024">
        <path
                d="M644.032 224a128 128 0 1 1 0 64H288a96 96 0 1 0 0 192H384v64H288a160 160 0 0 1 0-320h356.032zM768 320a64 64 0 1 0 0-128 64 64 0 0 0 0 128z">
        </path>
        <path d="M448 512a64 64 0 1 1 128 0 64 64 0 0 1-128 0z"></path>
        <path
                d="M256 640a128 128 0 1 0 123.968 160H736a160 160 0 0 0 0-320H640v64h96a96 96 0 0 1 0 192H379.968A128 128 0 0 0 256 640z m-64 128a64 64 0 1 1 128 0 64 64 0 0 1-128 0z">
        </path>
    </symbol>
    <symbol id="iconnav-logout" viewBox="0 0 1024 1024">
        <path
                d="M782.938353 145.167059a30.117647 30.117647 0 0 0-29.635765-24.696471H150.588235l-5.421176 0.481883A30.117647 30.117647 0 0 0 120.470588 150.588235v662.889412l0.481883 5.421177a30.117647 30.117647 0 0 0 29.635764 24.69647h602.714353l5.421177-0.481882a30.117647 30.117647 0 0 0 24.69647-29.635765v-105.472l-0.481882-5.421176a30.117647 30.117647 0 0 0-29.635765-24.696471l-5.421176 0.481882a30.117647 30.117647 0 0 0-24.696471 29.635765v75.294118H180.705882V180.705882h542.479059v75.414589l0.481883 5.421176a30.117647 30.117647 0 0 0 59.753411-5.421176V150.588235l-0.481882-5.421176z">
        </path>
        <path
                d="M802.032941 421.888a30.117647 30.117647 0 0 1 48.067765-24.154353l81.136941 60.416a30.117647 30.117647 0 0 1 0 48.308706L850.100706 566.814118a30.117647 30.117647 0 0 1-48.067765-24.214589v-30.599529H362.014118a30.117647 30.117647 0 0 1 0-60.235294h440.018823v-29.936941z">
        </path>
    </symbol>
    <symbol id="iconnav-fold" viewBox="0 0 1024 1024">
        <path
                d="M895.424 127.168H64v128h831.424v-128zM640.192 511.168l255.808 128v-256l-255.808 128zM64 447.168h511.68v128H64v-128zM895.424 767.168H64v128h831.424v-128z">
        </path>
    </symbol>
    <symbol id="iconnav-serverInfo" viewBox="0 0 1024 1024">
        <path
                d="M541.394824 394.842353a33.129412 33.129412 0 0 0 24.094117-9.758118 31.984941 31.984941 0 0 0 9.938824-23.732706 31.984941 31.984941 0 0 0-9.938824-23.732705 33.129412 33.129412 0 0 0-24.094117-9.758118 33.189647 33.189647 0 0 0-23.973648 9.758118 31.864471 31.864471 0 0 0-10.059294 23.732705c0 9.336471 3.312941 17.227294 10.059294 23.732706a33.189647 33.189647 0 0 0 23.973648 9.758118zM573.44 744.869647V450.56H509.168941v294.249412H573.44z">
        </path>
        <path
                d="M542.117647 120.470588a421.647059 421.647059 0 1 1 0 843.294118A421.647059 421.647059 0 0 1 542.117647 120.470588z m0 60.235294a361.411765 361.411765 0 1 0 0 722.82353A361.411765 361.411765 0 0 0 542.117647 180.705882z">
        </path>
    </symbol>
    <symbol id="iconnav-operationRecord" viewBox="0 0 1024 1024">
        <path
                d="M192 96A96 96 0 0 0 96 192v640A96 96 0 0 0 192 928h384v-64H192a32 32 0 0 1-32-32V192a32 32 0 0 1 32-32h576a32 32 0 0 1 32 32v288h64V192A96 96 0 0 0 768 96H192z">
        </path>
        <path
                d="M256 352h448v-64H256v64zM256 544h448v-64H256v64zM256 736h224v-64H256v64zM967.04 569.728a96 96 0 0 0-140.16 4.672L640 787.968V928h142.528l194.112-221.888a96 96 0 0 0-4.352-131.072l-5.312-5.312z m-92.032 46.848a32 32 0 0 1 46.72-1.6l5.312 5.312a32 32 0 0 1 1.472 43.712L753.472 864H704v-51.968l171.008-195.456z">
        </path>
    </symbol>
    <symbol id="iconnav-ram" viewBox="0 0 1024 1024">
        <path
                d="M556.608 256a64 64 0 0 1 35.52 10.752l147.392 98.24a64 64 0 0 1 28.48 53.248V448a64 64 0 0 1-64 64H320a64 64 0 0 1-64-64V320a64 64 0 0 1 64-64h236.608zM704 418.24L556.608 320H320v128h384v-29.76z">
        </path>
        <path
                d="M618.688 128a64 64 0 0 1 38.4 12.8L870.4 300.736a64 64 0 0 1 25.6 51.2V832a64 64 0 0 1-64 64H192a64 64 0 0 1-64-64V192a64 64 0 0 1 64-64h426.688zM416 832v-128h64v128h64v-128h64v128h64v-128h64v128H832V351.936L618.688 192H192v640h96v-128h64v128h64z">
        </path>
    </symbol>
    <symbol id="iconnav-dashboard" viewBox="0 0 1024 1024">
        <path
                d="M779.456 266.368a32 32 0 0 1 53.632 34.432l-3.136 4.864-159.36 204.672a32 32 0 0 1-48.832 2.048l-3.584-4.736-46.336-73.92-103.424 82.048a32 32 0 0 1-35.712 2.752l-4.544-3.2-68.544-56.448-113.408 176.192a32 32 0 0 1-39.104 12.224l-5.12-2.624a32 32 0 0 1-12.288-39.104l2.688-5.12 132.864-206.464a32 32 0 0 1 42.112-10.88l5.12 3.52 76.416 62.976 111.232-88.128a32 32 0 0 1 43.2 3.136l3.84 4.928 40.96 65.408 131.328-168.576zM836.672 734.464a32 32 0 0 0-32-32H219.264l-5.76 0.576a32 32 0 0 0 5.76 63.488h585.408l5.76-0.512a32 32 0 0 0 26.24-31.488z">
        </path>
        <path
                d="M960.128 192.256v640.128a64 64 0 0 1-64 63.936H128a64 64 0 0 1-64-64v-640a64 64 0 0 1 64-64h768.128a64 64 0 0 1 64 64zM128 192.256v640.128h768.128V192.256H128z">
        </path>
    </symbol>
    <symbol id="iconnav-processManagement" viewBox="0 0 1024 1024">
        <path
                d="M542.336 288a32 32 0 0 1 30.272 17.664l76.16 152.32 32.64-32.64A32 32 0 0 1 704 416h128v64h-114.752l-54.592 54.656a32 32 0 0 1-51.264-8.32l-63.168-126.4-70.272 187.328a32 32 0 0 1-58.56 3.072L364.16 480H192v-64h192a32 32 0 0 1 28.608 17.664l31.168 62.4 70.272-187.328a32 32 0 0 1 28.288-20.672z">
        </path>
        <path
                d="M128 128a64 64 0 0 0-64 64v512a64 64 0 0 0 64 64h768a64 64 0 0 0 64-64V192a64 64 0 0 0-64-64H128z m768 64v512H128V192h768zM192 832a32 32 0 0 0 0 64h640a32 32 0 0 0 0-64H192z">
        </path>
    </symbol>
    <symbol id="iconpage-down" viewBox="0 0 1024 1024">
        <path
                d="M233.344 361.344a32 32 0 0 1 45.312 0L512 594.752l233.344-233.408a32 32 0 0 1 45.312 45.312l-256 256a32 32 0 0 1-45.312 0l-256-256a32 32 0 0 1 0-45.312z">
        </path>
    </symbol>
    <symbol id="iconnav-storage" viewBox="0 0 1024 1024">
        <path
                d="M95.872 320c0-113.664 187.968-192 416.128-192s416.128 78.336 416.128 192c0 113.792-187.968 192.128-416.128 192.128S95.872 433.792 95.872 320z m768.256 0c0-62.976-155.968-128-352.128-128S159.872 257.024 159.872 320c0 63.104 155.968 128.128 352.128 128.128S864.128 383.104 864.128 320z">
        </path>
        <path
                d="M896.128 480a32 32 0 0 1 32 32c0 113.728-187.968 192-416.128 192S95.872 625.792 95.872 512a32 32 0 0 1 63.488-5.76l0.512 5.76c0 63.04 155.968 128 352.128 128 190.208 0 342.656-61.056 351.744-122.24l0.384-5.76a32 32 0 0 1 32-32z">
        </path>
        <path
                d="M928.128 703.936a32 32 0 0 0-64 0l-0.384 5.76C854.656 770.88 702.208 832 512 832c-196.16 0-352.128-65.024-352.128-128.064l-0.512-5.76a32 32 0 0 0-63.488 5.76C95.872 817.664 283.84 896 512 896s416.128-78.336 416.128-192.064z">
        </path>
    </symbol>
    <symbol id="iconpage-left" viewBox="0 0 1024 1024">
        <path d="M685.248 195.2L640 149.952 277.952 512 640 874.048l45.248-45.248L368.448 512l316.8-316.8z"></path>
    </symbol>
    <symbol id="iconReduce" viewBox="0 0 1024 1024">
        <path d="M192 480m32 0l576 0q32 0 32 32l0 0q0 32-32 32l-576 0q-32 0-32-32l0 0q0-32 32-32Z"></path>
    </symbol>
    <symbol id="iconpage-up" viewBox="0 0 1024 1024">
        <path
                d="M790.656 662.656a32 32 0 0 1-45.312 0L512 429.248l-233.344 233.408a32 32 0 0 1-45.312-45.312l256-256a32 32 0 0 1 45.312 0l256 256a32 32 0 0 1 0 45.312z">
        </path>
    </symbol>
    <symbol id="iconoverload" viewBox="0 0 1024 1024">
        <path
                d="M417.024 312.896h189.888l-3.264 6.208C563.968 392.448 530.752 426.624 512 426.624l-2.944-0.192c-19.2-3.264-51.008-37.632-88.704-107.328l-3.328-6.208zM512 597.312c-18.752 0-51.968 34.24-91.648 107.584l-3.328 6.208h189.888l-3.264-6.208c-37.696-69.696-69.568-104.064-88.704-107.328L512 597.312z">
        </path>
        <path
                d="M1024 512A512 512 0 1 1 0 512a512 512 0 0 1 1024 0zM512 512c75.84 0 151.68-94.784 227.584-284.416H284.416C360.32 417.216 436.096 512 512 512z m0 0c-75.84 0-151.68 94.784-227.584 284.416h455.168C663.68 606.784 587.904 512 512 512z">
        </path>
    </symbol>
    <symbol id="iconPlus" viewBox="0 0 1024 1024">
        <path
                d="M544 224a32 32 0 0 0-64 0v256h-256a32 32 0 0 0 0 64h256v256a32 32 0 0 0 64 0v-256h256a32 32 0 0 0 0-64h-256v-256z">
        </path>
    </symbol>
    <symbol id="iconnav-machine" viewBox="0 0 1024 1024">
        <path d="M737.472 240a32 32 0 1 1 64 0 32 32 0 0 1-64 0zM679.744 208a32 32 0 1 0 0 64 32 32 0 0 0 0-64z"></path>
        <path
                d="M929.28 191.808a64 64 0 0 0-64-64H160.064a64 64 0 0 0-64 64v96.32a64 64 0 0 0 64 64H865.28a64 64 0 0 0 64-64V191.808z m-769.152 0H865.28v96.32H160.128V191.808z">
        </path>
        <path d="M769.472 480a32 32 0 1 0 0 64 32 32 0 0 0 0-64zM647.744 512a32 32 0 1 1 64 0 32 32 0 0 1-64 0z"></path>
        <path
                d="M929.28 463.808a64 64 0 0 0-64-64H160.064a64 64 0 0 0-64 64v96.32a64 64 0 0 0 64 64H865.28a64 64 0 0 0 64-64V463.808z m-769.152 0H865.28v96.32H160.128V463.808z">
        </path>
        <path d="M737.472 784.384a32 32 0 1 1 64 0 32 32 0 0 1-64 0zM679.744 752.384a32 32 0 1 0 0 64 32 32 0 0 0 0-64z">
        </path>
        <path
                d="M865.28 672.256a64 64 0 0 1 64 64v96.256a64 64 0 0 1-64 64H160.064a64 64 0 0 1-64-64v-96.256a64 64 0 0 1 64-64H865.28z m0 64H160.064v96.256H865.28v-96.256z">
        </path>
    </symbol>
    <symbol id="iconnav-unfoldTriangle" viewBox="0 0 1024 1024">
        <path
                d="M238.08 595.648c-40.32 40.32-11.712 109.248 45.312 109.248h459.904c56.96 0 85.568-68.928 45.248-109.248L558.592 365.696a64 64 0 0 0-90.496 0L238.08 595.648z">
        </path>
    </symbol>
    <symbol id="iconnav-unfold" viewBox="0 0 1024 1024">
        <path
                d="M64.576 127.168H896v128H64.576v-128zM319.808 511.168L64 639.168v-256l255.808 128zM896 447.168H384.32v128H896v-128zM64.576 767.168H896v128H64.576v-128z">
        </path>
    </symbol>
    <symbol id="iconnav-snapshot" viewBox="0 0 1088 1024">
        <path
                d="M711.296 549.76a200.128 200.128 0 1 0-400.32 0.064 200.128 200.128 0 0 0 400.32 0z m-336.32 0a136.128 136.128 0 1 1 272.256 0 136.128 136.128 0 0 1-272.256 0zM758.08 352.128a32 32 0 0 0 0 64h64a32 32 0 1 0 0-64h-64z">
        </path>
        <path
                d="M678.72 211.264a64 64 0 0 0-57.28-35.392H402.368a64 64 0 0 0-57.28 35.392l-19.456 39.04H128a64 64 0 0 0-64 64v469.824a64 64 0 0 0 64 64h768a64 64 0 0 0 64-64V314.304a64 64 0 0 0-64-64h-197.824l-19.456-39.04z m-57.28 28.608l37.12 74.432H896v469.824H128V314.304h237.184l37.184-74.432H621.44z">
        </path>
    </symbol>
    <symbol id="iconnormal" viewBox="0 0 1024 1024">
        <path
                d="M512 1024A512 512 0 1 0 512 0a512 512 0 0 0 0 1024zM250.496 543.872l80.512-80.448 120.64 120.704 241.344-241.344 80.512 80.448-321.856 321.792-201.152-201.152z">
        </path>
    </symbol>
    <symbol id="iconnav-timeConsuming" viewBox="0 0 1024 1024">
        <path
                d="M507.663059 311.838118a30.117647 30.117647 0 0 0-59.632941 6.445176l3.975529 224.677647 0.662588 5.662118a30.117647 30.117647 0 0 0 8.131765 14.998588l122.096941 122.759529 4.156235 3.493648a30.117647 30.117647 0 0 0 38.430118-3.373177l3.493647-4.156235a30.117647 30.117647 0 0 0-3.373176-38.430118L512 529.648941l-3.734588-212.389647-0.602353-5.421176z">
        </path>
        <path
                d="M481.882353 120.470588a421.647059 421.647059 0 1 1 0 843.294118A421.647059 421.647059 0 0 1 481.882353 120.470588z m0 60.235294a361.411765 361.411765 0 1 0 0 722.82353A361.411765 361.411765 0 0 0 481.882353 180.705882z">
        </path>
    </symbol>
    <symbol id="iconsearch" viewBox="0 0 1024 1024">
        <path
                d="M448 128a320 320 0 0 1 232.832 539.52l187.072 187.136-45.248 45.248-190.336-190.272A320 320 0 1 1 448 128z m0 64a256 256 0 1 0 0 512 256 256 0 0 0 0-512z">
        </path>
    </symbol>
    <symbol id="iconnav-serverControl" viewBox="0 0 1024 1024">
        <path
                d="M920.384 279.616L538.752 85.12a64 64 0 0 0-58.112 0L98.944 279.68a64 64 0 0 0 0 114.048L480.64 588.096a64 64 0 0 0 58.112 0l381.632-194.432a64 64 0 0 0 0-114.048zM509.696 142.08l381.632 194.496-381.632 194.432L128 336.64 509.696 142.08z">
        </path>
        <path
                d="M908.672 509.248a32 32 0 0 1 33.92 53.952l-4.864 3.072-413.504 210.688a32 32 0 0 1-23.424 2.24l-5.632-2.24-413.568-210.688a32 32 0 0 1 23.68-59.136l5.376 2.112 398.976 203.264 399.04-203.264z">
        </path>
        <path
                d="M951.68 692.224a32 32 0 0 0-43.008-13.952l-399.04 203.2-398.976-203.2-5.312-2.176a32 32 0 0 0-23.68 59.2l413.44 210.688 5.76 2.24a32 32 0 0 0 23.36-2.24l413.504-210.688 4.928-3.072a32 32 0 0 0 9.088-40z">
        </path>
    </symbol>
    <symbol id="iconreturn" viewBox="0 0 1088 1024">
        <path d="M0 512l448-448 73.472 73.472L198.08 460.8h741.248v105.6H203.712l311.68 311.616-74.752 74.624L0 512z">
        </path>
    </symbol>
    <symbol id="iconnav-net" viewBox="0 0 1024 1024">
        <path
                d="M512 128a384 384 0 1 1 0 768A384 384 0 0 1 512 128zM389.504 216.32A321.152 321.152 0 0 0 218.624 384h112.32c11.648-66.048 32.128-123.84 58.56-167.68zM396.032 384h231.936C605.568 269.376 555.776 192 512 192c-43.776 0-93.568 77.376-115.968 192zM322.624 448H198.4a321.472 321.472 0 0 0 0 128h124.224a766.208 766.208 0 0 1 0-128z m64.32 128h250.112a695.936 695.936 0 0 0 0-128H386.944a695.936 695.936 0 0 0 0 128z m-56 64H218.624a321.152 321.152 0 0 0 170.88 167.68c-26.432-43.84-46.912-101.568-58.56-167.68z m303.552 167.68a321.152 321.152 0 0 0 170.88-167.68h-112.32c-11.648 66.112-32.128 123.84-58.56 167.68zM627.968 640H396.032c22.4 114.624 72.192 192 115.968 192 43.776 0 93.568-77.376 115.968-192z m73.408-64H825.6a321.408 321.408 0 0 0 0-128h-124.224a765.696 765.696 0 0 1 0 128z m-8.32-192h112.32a321.152 321.152 0 0 0-170.88-167.68c26.432 43.84 46.912 101.632 58.56 167.68z">
        </path>
    </symbol>
    <symbol id="iconpage-right" viewBox="0 0 1024 1024">
        <path d="M338.752 195.2L384 149.952 746.048 512 384 874.048l-45.248-45.248L655.552 512l-316.8-316.8z"></path>
    </symbol>
    <symbol id="iconservice-status" viewBox="0 0 1024 1024">
        <path
                d="M64 128a64 64 0 0 1 64-64h768a64 64 0 0 1 64 64v576a64 64 0 0 1-64 64H128a64 64 0 0 1-64-64V128z m128 384v128h128V512H192z m384-128H448v256h128V384z m128-128v384h128V256h-128zM256 832a64 64 0 1 0 0 128h512a64 64 0 1 0 0-128H256z">
        </path>
    </symbol>
    <symbol id="iconservice-meta" viewBox="0 0 1024 1024">
        <path d="M512 698.88a186.88 186.88 0 1 0 0-373.76 186.88 186.88 0 0 0 0 373.76z"></path>
        <path
                d="M558.72 12.544l362.496 209.28c28.928 16.64 46.72 47.488 46.72 80.896v418.56a93.44 93.44 0 0 1-46.72 80.896l-362.496 209.28a93.44 93.44 0 0 1-93.44 0l-362.496-209.28a93.44 93.44 0 0 1-46.72-80.896V302.72c0-33.408 17.792-64.192 46.72-80.896L465.28 12.544a93.44 93.44 0 0 1 93.44 0z m315.776 290.176L512 93.44 149.504 302.72v418.56L512 930.56l362.496-209.28V302.72z">
        </path>
    </symbol>
    <symbol id="iconservice-graph" viewBox="0 0 1024 1024">
        <path
                d="M882.944 298.688a128 128 0 1 0-107.136-57.984l-116.48 109.76a233.92 233.92 0 0 0-147.072-51.84c-55.872 0-107.2 19.52-147.52 52.096L248.192 240.768a128 128 0 1 0-47.488 43.2l120.192 113.28c-27.456 38.464-43.52 85.504-43.52 136.32 0 50.688 16 97.664 43.328 136.064l-103.36 97.472a128 128 0 1 0 40.32 49.92l106.88-100.736c40.32 32.64 91.776 52.224 147.712 52.224 55.808 0 107.072-19.52 147.392-52.032l106.688 100.48a128 128 0 1 0 40.32-49.92l-103.04-97.152c27.392-38.4 43.52-85.504 43.52-136.32 0-50.944-16.192-98.112-43.776-136.64l119.936-113.024c17.792 9.408 38.08 14.72 59.648 14.72zM627.968 380.032l-47.744 45.056a127.36 127.36 0 0 0-67.968-19.52c-25.152 0-48.64 7.232-68.352 19.776l-47.744-44.992a191.36 191.36 0 0 1 116.096-39.04 191.36 191.36 0 0 1 115.712 38.72z m-3.712 91.52l47.68-44.992a191.36 191.36 0 0 1 32.576 107.008 191.36 191.36 0 0 1-32.32 106.752l-47.808-44.992c10.112-18.304 15.872-39.36 15.872-61.76 0-22.528-5.824-43.648-16-62.08z m-112 254.272a191.36 191.36 0 0 1-116.288-39.168l47.744-44.992c19.84 12.608 43.328 19.904 68.48 19.904a127.36 127.36 0 0 0 68.288-19.712l47.744 45.056a191.36 191.36 0 0 1-115.968 38.912z m-128-192.256c0 22.272 5.696 43.2 15.68 61.44L352.128 640A191.36 191.36 0 0 1 320 533.568c0-39.424 11.904-76.16 32.256-106.624l47.744 44.992a127.36 127.36 0 0 0-15.744 61.632z">
        </path>
    </symbol>
    <symbol id="iconup" viewBox="0 0 1024 1024">
        <path d="M512 64L64 504.448 136.768 576l320.64-315.328v700.16h109.12V260.608L887.232 576 960 504.448 512 64z">
        </path>
    </symbol>
    <symbol id="iconWrong" viewBox="0 0 1024 1024">
        <path
                d="M738.304 331.008a32 32 0 1 0-45.312-45.312L512 466.752 331.008 285.696a32 32 0 0 0-45.312 45.312L466.752 512l-181.056 180.992a32 32 0 1 0 45.312 45.312L512 557.248l180.992 181.056a32 32 0 1 0 45.312-45.312L557.248 512l181.056-180.992z">
        </path>
    </symbol>
    <symbol id="iconservice-slide-a" viewBox="0 0 1024 1024">
        <path
                d="M352.64 704a64 64 0 0 1-64 64h-64a64 64 0 0 1-64-64V320a64 64 0 0 1 64-64h64a64 64 0 0 1 64 64v384z m-128-384v384h64V320h-64z m256 0v384h64V320h-64z m-64 0a64 64 0 0 1 64-64h64a64 64 0 0 1 64 64v384a64 64 0 0 1-64 64h-64a64 64 0 0 1-64-64V320z m384 0v384h-64V320h64z m-64-64a64 64 0 0 0-64 64v384a64 64 0 0 0 64 64h64a64 64 0 0 0 64-64V320a64 64 0 0 0-64-64h-64z">
        </path>
    </symbol>
    <symbol id="iconservice-arrange-b" viewBox="0 0 1024 1024">
        <path
                d="M287.424 320h-64v64h64V320zM287.424 640h-64v64h64v-64zM479.424 640h64v64h-64v-64zM799.424 640h-64v64h64v-64zM479.424 320h64v64h-64V320zM799.424 320h-64v64h64V320z">
        </path>
        <path
                d="M192 0a192 192 0 0 0-192 192v640a192 192 0 0 0 192 192h640a192 192 0 0 0 192-192V192a192 192 0 0 0-192-192H192z m159.424 320v64a64 64 0 0 1-64 64h-64a64 64 0 0 1-64-64V320a64 64 0 0 1 64-64h64a64 64 0 0 1 64 64z m-64 256a64 64 0 0 1 64 64v64a64 64 0 0 1-64 64h-64a64 64 0 0 1-64-64v-64a64 64 0 0 1 64-64h64z m256 0a64 64 0 0 1 64 64v64a64 64 0 0 1-64 64h-64a64 64 0 0 1-64-64v-64a64 64 0 0 1 64-64h64z m320 64v64a64 64 0 0 1-64 64h-64a64 64 0 0 1-64-64v-64a64 64 0 0 1 64-64h64a64 64 0 0 1 64 64z m-320-384a64 64 0 0 1 64 64v64a64 64 0 0 1-64 64h-64a64 64 0 0 1-64-64V320a64 64 0 0 1 64-64h64z m320 64v64a64 64 0 0 1-64 64h-64a64 64 0 0 1-64-64V320a64 64 0 0 1 64-64h64a64 64 0 0 1 64 64z">
        </path>
    </symbol>
    <symbol id="iconservice-arrange-a" viewBox="0 0 1024 1024">
        <path
                d="M607.424 384a64 64 0 0 1-64 64h-64a64 64 0 0 1-64-64V320a64 64 0 0 1 64-64h64a64 64 0 0 1 64 64v64z m-256 0V320a64 64 0 0 0-64-64h-64a64 64 0 0 0-64 64v64a64 64 0 0 0 64 64h64a64 64 0 0 0 64-64z m-64-64v64h-64V320h64z m0 320v64h-64v-64h64z m-64-64a64 64 0 0 0-64 64v64a64 64 0 0 0 64 64h64a64 64 0 0 0 64-64v-64a64 64 0 0 0-64-64h-64z m256-256v64h64V320h-64z m64 320v64h-64v-64h64z m-64-64a64 64 0 0 0-64 64v64a64 64 0 0 0 64 64h64a64 64 0 0 0 64-64v-64a64 64 0 0 0-64-64h-64z m256-256v64h64V320h-64z m-64 0a64 64 0 0 1 64-64h64a64 64 0 0 1 64 64v64a64 64 0 0 1-64 64h-64a64 64 0 0 1-64-64V320z m128 320v64h-64v-64h64z m-64-64a64 64 0 0 0-64 64v64a64 64 0 0 0 64 64h64a64 64 0 0 0 64-64v-64a64 64 0 0 0-64-64h-64z">
        </path>
    </symbol>
    <symbol id="iconservice-slide-b" viewBox="0 0 1024 1024">
        <path d="M289.856 320h-64v384h64V320zM481.856 320h64v384h-64V320zM801.856 320h-64v384h64V320z"></path>
        <path
                d="M192 0a192 192 0 0 0-192 192v640a192 192 0 0 0 192 192h640a192 192 0 0 0 192-192V192a192 192 0 0 0-192-192H192z m97.856 256a64 64 0 0 1 64 64v384a64 64 0 0 1-64 64h-64a64 64 0 0 1-64-64V320a64 64 0 0 1 64-64h64z m256 0a64 64 0 0 1 64 64v384a64 64 0 0 1-64 64h-64a64 64 0 0 1-64-64V320a64 64 0 0 1 64-64h64z m320 64v384a64 64 0 0 1-64 64h-64a64 64 0 0 1-64-64V320a64 64 0 0 1 64-64h64a64 64 0 0 1 64 64z">
        </path>
    </symbol>
    <symbol id="iconnav-language" viewBox="0 0 1024 1024">
        <path
                d="M512 16a496 496 0 1 0 0 992A496 496 0 0 0 512 16zM192.448 271.36v97.6L332.8 545.088h123.968v-123.52l123.456-59.136V255.872l103.296-78.912 12.16-20.352a401.216 401.216 0 0 1 195.84 228.864L768 426.688v138.624l123.904 72.32A401.024 401.024 0 0 1 640 891.008v-90.624l-192-126.848-64 93.312v124.16a400.192 400.192 0 0 1-191.552-619.648z">
        </path>
    </symbol>
    <symbol id="iconSetting" viewBox="0 0 1024 1024">
        <path
                d="M620.8 437.76a224 224 0 0 0-345.28-254.72l113.728 113.728a6.4 6.4 0 0 1 1.856 4.48v81.536a6.4 6.4 0 0 1-6.4 6.4H303.232a6.4 6.4 0 0 1-4.48-1.92L184.96 273.6a224 224 0 0 0 254.72 345.216l241.472 241.536a64 64 0 0 0 90.56 0l90.496-90.56a64 64 0 0 0 0-90.496L620.736 437.76z m-414.72-52.608l47.36 47.36a70.4 70.4 0 0 0 49.792 20.672h81.472a70.4 70.4 0 0 0 70.4-70.4V301.312a70.4 70.4 0 0 0-20.608-49.792l-47.424-47.36a160 160 0 0 1 173.44 212.224l-13.632 38.08 270.08 270.08-90.496 90.496-270.08-270.08-38.144 13.568a160 160 0 0 1-212.16-173.44z">
        </path>
    </symbol>
    <symbol id="iconservice-storage" viewBox="0 0 1024 1024">
        <path
                d="M0 170.688C0 111.808 229.248 64 512 64s512 47.744 512 106.688V320c0 58.88-229.248 106.688-512 106.688S0 378.88 0 320V170.688zM512 533.312c282.752 0 512-47.744 512-106.624V576c0 58.88-229.248 106.688-512 106.688S0 634.88 0 576V426.688c0 58.88 229.248 106.624 512 106.624zM1024 682.688c0 58.88-229.248 106.624-512 106.624s-512-47.744-512-106.624V832c0 58.88 229.248 106.688 512 106.688s512-47.744 512-106.688v-149.312z">
        </path>
    </symbol>
    <symbol id="iconSetup" viewBox="0 0 1024 1024">
        <path
                d="M817.28 260.224l-53.632-53.504-4.864-4.224a50.56 50.56 0 0 0-66.56 4.288L414.912 484.736a50.24 50.24 0 0 0-13.248 23.68l-17.088 70.592a50.432 50.432 0 0 0 60.8 60.672l70.656-17.024a50.432 50.432 0 0 0 23.872-13.44l277.44-277.952a50.176 50.176 0 0 0-0.128-71.04z m-146.304 44.416l48.576-48.64 48.448 48.32-48.448 48.512-48.64-48.192z m-45.248 45.312l48.64 48.192-163.392 163.584-63.808 15.36 15.424-63.744 163.2-163.392z">
        </path>
        <path
                d="M482.88 192a29.12 29.12 0 0 1 0 58.24H269.568a19.392 19.392 0 0 0-19.008 15.424l-0.384 3.904v484.864c0 9.344 6.656 17.152 15.488 19.008l3.904 0.384h484.864a19.392 19.392 0 0 0 19.008-15.488l0.384-3.904V541.12a29.12 29.12 0 0 1 58.176 0v213.312c0 40.32-30.72 73.472-70.08 77.248l-7.488 0.32H269.568c-40.32 0-73.472-30.72-77.184-70.08L192 754.432V269.568c0-40.32 30.72-73.472 70.08-77.184L269.568 192h213.312z">
        </path>
    </symbol>
    <symbol id="iconzoom" viewBox="0 0 1024 1024">
        <path
                d="M161.6 415.744c-18.56 0-33.6-13.888-33.6-31.04V160a30.272 30.272 0 0 1 12.672-24.576A33.792 33.792 0 0 1 162.304 128h242.56c18.56 0 33.664 13.952 33.664 31.104 0 17.088-15.04 31.04-33.6 31.04H236.672L439.232 396.16c12.48 12.736 11.52 32.256-2.24 43.776a35.392 35.392 0 0 1-47.36-2.048L195.2 240V384.64c0 17.088-15.104 31.04-33.6 31.04z m700.8-0.128c18.56 0 33.6-13.952 33.6-31.104V160a30.08 30.08 0 0 0-12.672-24.512 33.792 33.792 0 0 0-21.632-7.488h-242.56c-18.56 0-33.664 13.952-33.664 31.04 0 17.152 15.04 31.104 33.6 31.104h168.256L584.768 396.096a29.312 29.312 0 0 0 2.24 43.776c13.76 11.52 34.88 10.688 47.36-2.048l194.432-198.016v144.704c0 17.152 15.04 31.104 33.6 31.104zM128 639.296c0-17.152 15.104-31.04 33.6-31.04 18.56 0 33.6 13.952 33.6 31.04v144.768L389.632 586.24a35.392 35.392 0 0 1 47.36-2.048c13.824 11.52 14.72 31.04 2.24 43.776L236.672 833.92h168.256c18.56 0 33.6 13.952 33.6 31.04 0 17.152-15.104 31.104-33.6 31.104h-242.56a33.792 33.792 0 0 1-23.36-8.832A29.952 29.952 0 0 1 128 864v-224.64z m768 0v224.512a30.4 30.4 0 0 1-12.672 24.704 33.792 33.792 0 0 1-21.632 7.488h-242.56c-18.56 0-33.664-13.952-33.664-31.04 0-17.152 15.04-31.104 33.6-31.104h168.256L584.768 627.904a29.312 29.312 0 0 1 2.24-43.776 35.392 35.392 0 0 1 47.36 2.048l194.432 197.76V639.36c0-17.152 15.04-31.04 33.6-31.04 18.56 0 33.6 13.888 33.6 31.04z">
        </path>
    </symbol>
    <symbol id="iconwatch" viewBox="0 0 1024 1024">
        <path
                d="M512 379.968c-72.32 0-131.008 60.48-131.008 135.04 0 74.624 58.624 135.104 131.008 135.104 72.32 0 131.008-60.48 131.008-135.04 0-74.624-58.624-135.04-131.008-135.04z m0 66.304c36.864 0 66.752 30.784 66.752 68.8S548.864 583.872 512 583.872c-36.864 0-66.752-30.784-66.752-68.8S475.136 446.272 512 446.272z">
        </path>
        <path
                d="M512 227.072c-140.16 0-264.704 91.712-373.312 270.4L128 515.072l10.688 17.6C247.296 711.36 371.84 803.072 512 803.072c140.16 0 264.704-91.712 373.312-270.4L896 515.072l-10.688-17.6C776.704 318.72 652.16 227.072 512 227.072z m11.456 66.56c102.72 4.672 198.528 72.832 287.68 207.872l8.704 13.568-8.704 13.504C718.72 668.8 619.072 736.768 512 736.768l-11.456-0.256C397.824 731.84 302.08 663.68 212.864 528.64l-8.768-13.504 8.768-13.568C305.28 361.408 404.928 293.312 512 293.312l11.456 0.256z">
        </path>
    </symbol>
    <symbol id="iconnav-service" viewBox="0 0 1024 1024">
        <path
                d="M658.176 104.512a64 64 0 0 1 64 64v238.912a64 64 0 0 1-64 64h-107.52v68.48h229.888A96 96 0 0 1 876.16 626.56l0.384 9.216v38.848h26.368a64 64 0 0 1 64 64v116.8a64 64 0 0 1-64 64h-116.736a64 64 0 0 1-64-64v-116.8a64 64 0 0 1 64-64h26.368v-38.848a32 32 0 0 0-26.24-31.488l-5.76-0.512H550.592v70.848h26.368a64 64 0 0 1 64 64v116.8a64 64 0 0 1-64 64H460.224a64 64 0 0 1-64-64v-116.8a64 64 0 0 1 64-64h26.368V603.904H256.64a32 32 0 0 0-31.488 26.24l-0.512 5.76v38.848h26.368a64 64 0 0 1 64 64v116.8a64 64 0 0 1-64 64H134.272a64 64 0 0 1-64-64v-116.8a64 64 0 0 1 64-64h26.368v-38.848a96 96 0 0 1 86.784-95.552l9.216-0.448h229.952V471.424h-107.52a64 64 0 0 1-64-64V168.512a64 64 0 0 1 64-64h279.104z m0 64H379.008v238.912h279.168V168.512z m-407.168 570.24H134.272v116.736h116.736v-116.8z m209.28 0v116.736h116.672v-116.8H460.224z m442.624 0h-116.736v116.736h116.736v-116.8z">
        </path>
    </symbol>
    <symbol id="iconservice" viewBox="0 0 1024 1024">
        <path
                d="M96 64a64 64 0 0 0-64 64v128a64 64 0 0 0 64 64h832a64 64 0 0 0 64-64V128a64 64 0 0 0-64-64h-832z m704 192a64 64 0 1 1 0-128 64 64 0 0 1 0 128zM96 384a64 64 0 0 0-64 64v128a64 64 0 0 0 64 64h832a64 64 0 0 0 64-64V448a64 64 0 0 0-64-64h-832z m704 192a64 64 0 1 1 0-128 64 64 0 0 1 0 128zM32 768a64 64 0 0 1 64-64h832a64 64 0 0 1 64 64v128a64 64 0 0 1-64 64h-832a64 64 0 0 1-64-64v-128z m832 64a64 64 0 1 0-128 0 64 64 0 0 0 128 0z">
        </path>
    </symbol>
</svg>
<div id="app" style="height: 100%;">
    <section class="ant-layout efak-stat">
        <div class="efak-stat-header">
            <ul class="ant-menu-overflow ant-menu ant-menu-root ant-menu-horizontal ant-menu-dark menu-top" role="menu"
                tabindex="0" data-menu-list="true">

                <li
                        class="ant-menu-overflow-item ant-menu-overflow-item-rest ant-menu-submenu ant-menu-submenu-horizontal ant-menu-submenu-disabled"
                        aria-hidden="true" role="none"
                        style="opacity: 0; height: 0px; overflow-y: hidden; order: 2147483647; pointer-events: none; position: absolute;">
                    <div role="menuitem" class="ant-menu-submenu-title" aria-expanded="false" aria-haspopup="true"
                         data-menu-id="rc-menu-uuid-35000-1-rc-menu-more"
                         aria-controls="rc-menu-uuid-35000-1-rc-menu-more-popup"
                         aria-disabled="true"><span role="img" aria-label="ellipsis" class="anticon anticon-ellipsis"><svg
                            viewBox="64 64 896 896" focusable="false" data-icon="ellipsis" width="1em" height="1em"
                            fill="currentColor" aria-hidden="true">
                  <path
                          d="M176 511a56 56 0 10112 0 56 56 0 10-112 0zm280 0a56 56 0 10112 0 56 56 0 10-112 0zm280 0a56 56 0 10112 0 56 56 0 10-112 0z">
                  </path>
                </svg></span><i class="ant-menu-submenu-arrow"></i></div>
                </li>
            </ul>
            <div aria-hidden="true" style="display: none;"></div>
            <ul class="ant-menu-overflow ant-menu ant-menu-root ant-menu-horizontal ant-menu-dark menu-top menu-right"
                role="menu" tabindex="0" data-menu-list="true">

                <li
                        class="ant-menu-overflow-item ant-menu-overflow-item-rest ant-menu-submenu ant-menu-submenu-horizontal ant-menu-submenu-disabled"
                        aria-hidden="true" role="none"
                        style="opacity: 0; height: 0px; overflow-y: hidden; order: 2147483647; pointer-events: none; position: absolute;">
                    <div role="menuitem" class="ant-menu-submenu-title" aria-expanded="false" aria-haspopup="true"
                         data-menu-id="rc-menu-uuid-35000-2-rc-menu-more"
                         aria-controls="rc-menu-uuid-35000-2-rc-menu-more-popup"
                         aria-disabled="true"><span role="img" aria-label="ellipsis" class="anticon anticon-ellipsis"><svg
                            viewBox="64 64 896 896" focusable="false" data-icon="ellipsis" width="1em" height="1em"
                            fill="currentColor" aria-hidden="true">
                  <path
                          d="M176 511a56 56 0 10112 0 56 56 0 10-112 0zm280 0a56 56 0 10112 0 56 56 0 10-112 0zm280 0a56 56 0 10112 0 56 56 0 10-112 0z">
                  </path>
                </svg></span><i class="ant-menu-submenu-arrow"></i></div>
                </li>
            </ul>
            <div aria-hidden="true" style="display: none;"></div>
        </div>
        <section class="ant-layout">
            <section class="ant-layout page-content" id="page-content">
                <section class="ant-layout efak-stat">
                    <section class="ant-layout ant-layout-has-sider">
                        <aside class="ant-layout-sider ant-layout-sider-dark efak-sider"
                               style="flex: 0 0 200px; max-width: 200px; min-width: 200px; width: 200px;">

                        </aside>
                        <section class="ant-layout page-content">
                            <main class="ant-layout-content main-content">
                                <div class="monitor-screen__aEWsg fullScreen__Dvo5p">
                                    <svg class="dashboard-icon zoom__zn9GT"
                                         aria-hidden="true">
                                        <!-- <use xlink:href="#iconzoom-in"></use> -->
                                    </svg>
                                    <div class="header-main__PtL2d">
                                        <div class="left-bg__USvfv"></div>
                                        <div class="right-bg__AHrI4"></div>
                                        <h3 class="title__SPK7K">EFAK&nbsp;&nbsp;TV&nbsp;&nbsp;Monitor</h3><img
                                            class="left-decoration__rpFqZ"
                                            src="/media/css/tv/images/efak01.webp"
                                            style="width: 200px; height: 30px;"><img class="right-decoration__qPCfj"
                                                                                     src="/media/css/tv/images/efak01.webp"
                                                                                     style="width: 200px; height: 30px;">
                                    </div>
                                    <div class="ant-row metrics__MRS58"
                                         style="margin-left: -12px; margin-right: -12px; row-gap: 0px;">
                                        <div class="ant-col ant-col-5" style="padding-left: 12px; padding-right: 12px;">
                                            <div class="dv-border-box-11 border-box__piX21" style="height: 260px;">
                                                <svg
                                                        class="dv-border-svg-container" width="375" height="260">
                                                    <defs>
                                                        <filter id="border-box-11-filterId-5fdd71a5f6d04bdb999b4db97e1b3a86"
                                                                height="150%"
                                                                width="150%" x="-25%" y="-25%">
                                                            <femorphology operator="dilate" radius="2" in="SourceAlpha"
                                                                          result="thicken">
                                                            </femorphology>
                                                            <fegaussianblur in="thicken" stdDeviation="3"
                                                                            result="blurred"></fegaussianblur>
                                                            <feflood flood-color="#235fa7" result="glowColor"></feflood>
                                                            <fecomposite in="glowColor" in2="blurred" operator="in"
                                                                         result="softGlowColored">
                                                            </fecomposite>
                                                            <femerge>
                                                                <femergenode in="softGlowColored"></femergenode>
                                                                <femergenode in="SourceGraphic"></femergenode>
                                                            </femerge>
                                                        </filter>
                                                    </defs>
                                                    <polygon fill="transparent" points="
          20, 32 62.5, 32 82.5, 53
          292.5, 53 312.5, 32
          355, 32 367, 48 367, 235 355, 252
          20, 252 8, 235 8, 50
        "></polygon>
                                                    <polyline stroke="#4fd2dd"
                                                              filter="url(#border-box-11-filterId-5fdd71a5f6d04bdb999b4db97e1b3a86)"
                                                              points="
          62.5, 30
          20, 30 7, 50 7, 96.5
          13, 101.5 13, 181.5
          7, 186.5 7, 233
          20, 253 355, 253 368, 233
          368, 186.5 362, 181.5
          362, 101.5 368, 96.5
          368, 50 355, 30 312.5, 30
          292.5, 7 82.5, 7
          62.5, 30 82.5, 52
          292.5, 52 312.5, 30
        "></polyline>
                                                    <polygon stroke="#4fd2dd" fill="transparent" points="
          307.5, 30 291.5, 11
          285.5, 11 304.5, 34
        "></polygon>
                                                    <polygon stroke="#4fd2dd" fill="transparent" points="
          67.5, 30 84.5, 49
          90.5, 49 70.5, 26
        "></polygon>
                                                    <polygon stroke="#4fd2dd" fill="rgba(35,95,167,0.3)"
                                                             filter="url(#border-box-11-filterId-5fdd71a5f6d04bdb999b4db97e1b3a86)"
                                                             points="
          301.5, 37 280.5, 11
          85.5, 11 73.5, 23
          95.5, 49 290.5, 49
        "></polygon>
                                                    <polygon
                                                            filter="url(#border-box-11-filterId-5fdd71a5f6d04bdb999b4db97e1b3a86)"
                                                            fill="#4fd2dd" opacity="1" points="
          52.5, 37 31.5, 37
          37.5, 46 58.5, 46
        "></polygon>
                                                    <polygon
                                                            filter="url(#border-box-11-filterId-5fdd71a5f6d04bdb999b4db97e1b3a86)"
                                                            fill="#4fd2dd" opacity="0.7" points="
          22.5, 37 1.5, 37
          7.5, 46 28.5, 46
        "></polygon>
                                                    <polygon
                                                            filter="url(#border-box-11-filterId-5fdd71a5f6d04bdb999b4db97e1b3a86)"
                                                            fill="#4fd2dd" opacity="0.5" points="
          -7.5, 37 -28.5, 37
          -22.5, 46 -1.5, 46
        "></polygon>
                                                    <polygon
                                                            filter="url(#border-box-11-filterId-5fdd71a5f6d04bdb999b4db97e1b3a86)"
                                                            fill="#4fd2dd" opacity="1" points="
          342.5, 37 321.5, 37
          315.5, 46 336.5, 46
        "></polygon>
                                                    <polygon
                                                            filter="url(#border-box-11-filterId-5fdd71a5f6d04bdb999b4db97e1b3a86)"
                                                            fill="#4fd2dd" opacity="0.7" points="
          372.5, 37 351.5, 37
          345.5, 46 366.5, 46
        "></polygon>
                                                    <polygon
                                                            filter="url(#border-box-11-filterId-5fdd71a5f6d04bdb999b4db97e1b3a86)"
                                                            fill="#4fd2dd" opacity="0.5" points="
          402.5, 37 381.5, 37
          375.5, 46 396.5, 46
        "></polygon>
                                                    <text class="dv-border-box-11-title" x="187.5" y="32" fill="#fff"
                                                          font-size="18"
                                                          text-anchor="middle" dominant-baseline="middle">Failed Fetch
                                                        Requests
                                                    </text>
                                                    <polygon fill="#4fd2dd"
                                                             filter="url(#border-box-11-filterId-5fdd71a5f6d04bdb999b4db97e1b3a86)"
                                                             points="
          7, 99.5 11, 103.5
          11, 179.5 7, 183.5
        "></polygon>
                                                    <polygon fill="#4fd2dd"
                                                             filter="url(#border-box-11-filterId-5fdd71a5f6d04bdb999b4db97e1b3a86)"
                                                             points="
          368, 99.5 364, 103.5
          364, 179.5 368, 183.5
        "></polygon>
                                                </svg>
                                                <div class="border-box-content">
                                                    <div id="efak_dashboard_faild_fetch_request_lastest"
                                                         class="chart-title__nQpiI" style="margin-top: 20%">[ 0
                                                        (MSG/min)]
                                                    </div>
                                                    <div class="screen-chart__nZcqk"
                                                         id="efak_dashboard_failed_fetch_request_chart"
                                                         style="position:relative;width: 80%;margin-left:10%;margin-top: 20%">
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="dv-border-box-11 storage-metrics__gilPc" style="height: 527px;">
                                                <svg
                                                        class="dv-border-svg-container" width="375" height="527">
                                                    <defs>
                                                        <filter id="border-box-11-filterId-7915575ec2774cd2a0032a601368cde1"
                                                                height="150%"
                                                                width="150%" x="-25%" y="-25%">
                                                            <femorphology operator="dilate" radius="2" in="SourceAlpha"
                                                                          result="thicken">
                                                            </femorphology>
                                                            <fegaussianblur in="thicken" stdDeviation="3"
                                                                            result="blurred"></fegaussianblur>
                                                            <feflood flood-color="#235fa7" result="glowColor"></feflood>
                                                            <fecomposite in="glowColor" in2="blurred" operator="in"
                                                                         result="softGlowColored">
                                                            </fecomposite>
                                                            <femerge>
                                                                <femergenode in="softGlowColored"></femergenode>
                                                                <femergenode in="SourceGraphic"></femergenode>
                                                            </femerge>
                                                        </filter>
                                                    </defs>
                                                    <polygon fill="transparent" points="
          20, 32 62.5, 32 82.5, 53
          292.5, 53 312.5, 32
          355, 32 367, 48 367, 502 355, 519
          20, 519 8, 502 8, 50
        "></polygon>
                                                    <polyline stroke="#4fd2dd"
                                                              filter="url(#border-box-11-filterId-7915575ec2774cd2a0032a601368cde1)"
                                                              points="
          62.5, 30
          20, 30 7, 50 7, 230
          13, 235 13, 315
          7, 320 7, 500
          20, 520 355, 520 368, 500
          368, 320 362, 315
          362, 235 368, 230
          368, 50 355, 30 312.5, 30
          292.5, 7 82.5, 7
          62.5, 30 82.5, 52
          292.5, 52 312.5, 30
        "></polyline>
                                                    <polygon stroke="#4fd2dd" fill="transparent" points="
          307.5, 30 291.5, 11
          285.5, 11 304.5, 34
        "></polygon>
                                                    <polygon stroke="#4fd2dd" fill="transparent" points="
          67.5, 30 84.5, 49
          90.5, 49 70.5, 26
        "></polygon>
                                                    <polygon stroke="#4fd2dd" fill="rgba(35,95,167,0.3)"
                                                             filter="url(#border-box-11-filterId-7915575ec2774cd2a0032a601368cde1)"
                                                             points="
          301.5, 37 280.5, 11
          85.5, 11 73.5, 23
          95.5, 49 290.5, 49
        "></polygon>
                                                    <polygon
                                                            filter="url(#border-box-11-filterId-7915575ec2774cd2a0032a601368cde1)"
                                                            fill="#4fd2dd" opacity="1" points="
          52.5, 37 31.5, 37
          37.5, 46 58.5, 46
        "></polygon>
                                                    <polygon
                                                            filter="url(#border-box-11-filterId-7915575ec2774cd2a0032a601368cde1)"
                                                            fill="#4fd2dd" opacity="0.7" points="
          22.5, 37 1.5, 37
          7.5, 46 28.5, 46
        "></polygon>
                                                    <polygon
                                                            filter="url(#border-box-11-filterId-7915575ec2774cd2a0032a601368cde1)"
                                                            fill="#4fd2dd" opacity="0.5" points="
          -7.5, 37 -28.5, 37
          -22.5, 46 -1.5, 46
        "></polygon>
                                                    <polygon
                                                            filter="url(#border-box-11-filterId-7915575ec2774cd2a0032a601368cde1)"
                                                            fill="#4fd2dd" opacity="1" points="
          342.5, 37 321.5, 37
          315.5, 46 336.5, 46
        "></polygon>
                                                    <polygon
                                                            filter="url(#border-box-11-filterId-7915575ec2774cd2a0032a601368cde1)"
                                                            fill="#4fd2dd" opacity="0.7" points="
          372.5, 37 351.5, 37
          345.5, 46 366.5, 46
        "></polygon>
                                                    <polygon
                                                            filter="url(#border-box-11-filterId-7915575ec2774cd2a0032a601368cde1)"
                                                            fill="#4fd2dd" opacity="0.5" points="
          402.5, 37 381.5, 37
          375.5, 46 396.5, 46
        "></polygon>
                                                    <text class="dv-border-box-11-title" x="187.5" y="32" fill="#fff"
                                                          font-size="18"
                                                          text-anchor="middle" dominant-baseline="middle">Fetch &
                                                        Produce
                                                    </text>
                                                    <polygon fill="#4fd2dd"
                                                             filter="url(#border-box-11-filterId-7915575ec2774cd2a0032a601368cde1)"
                                                             points="
          7, 233 11, 237
          11, 313 7, 317
        "></polygon>
                                                    <polygon fill="#4fd2dd"
                                                             filter="url(#border-box-11-filterId-7915575ec2774cd2a0032a601368cde1)"
                                                             points="
          368, 233 364, 237
          364, 313 368, 317
        "></polygon>
                                                </svg>
                                                <div class="border-box-content">
                                                    <div class="storage-chart-block__JPdjC">
                                                        <div class="chart-title__nQpiI">total fetch requests</div>
                                                        <div id="efak_dashboard_total_fetch_request_lastest"
                                                             class="chart-title__nQpiI" style="margin-top: 7%">
                                                            [
                                                            0
                                                            (MSG/min) ]
                                                        </div>
                                                        <div id="efak_dashboard_total_fetch_request_chart"
                                                             class="screen-chart__nZcqk"
                                                             style="width: 80%;margin-left: 10%"></div>
                                                    </div>
                                                    <div class="storage-chart-block__JPdjC">
                                                        <div class="chart-title__nQpiI">total produce requests</div>
                                                        <div id="efak_dashboard_total_produce_request_lastest"
                                                             class="chart-title__nQpiI" style="margin-top: 7%">
                                                            [
                                                            0
                                                            (MSG/min) ]
                                                        </div>
                                                        <div id="efak_dashboard_total_produce_request_chart"
                                                             class="screen-chart__nZcqk"
                                                             style="width: 80%;margin-left: 10%"></div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="ant-col ant-col-14"
                                             style="padding-left: 12px; padding-right: 12px;">
                                            <div class="center__Q6gqV" style="height: 787px;">
                                                <div class="dv-border-box-9 top-center__RWOom"
                                                     style="height: 432.85px;">
                                                    <svg
                                                            class="dv-border-svg-container" width="1093" height="433">
                                                        <defs>
                                                            <lineargradient
                                                                    id="border-box-9-gradient-6ae4fae4827845a29305b3df5332fe6b"
                                                                    x1="0%"
                                                                    y1="0%" x2="100%" y2="100%">
                                                                <animate attributeName="x1" values="0%;100%;0%"
                                                                         dur="10s" begin="0s"
                                                                         repeatCount="indefinite"></animate>
                                                                <animate attributeName="x2" values="100%;0%;100%"
                                                                         dur="10s" begin="0s"
                                                                         repeatCount="indefinite"></animate>
                                                                <stop offset="0%" stop-color="#4fd2dd">
                                                                    <animate attributeName="stop-color"
                                                                             values="#4fd2dd;#235fa7;#4fd2dd" dur="10s"
                                                                             begin="0s"
                                                                             repeatCount="indefinite"></animate>
                                                                </stop>
                                                                <stop offset="100%" stop-color="#235fa7">
                                                                    <animate attributeName="stop-color"
                                                                             values="#235fa7;#4fd2dd;#235fa7" dur="10s"
                                                                             begin="0s"
                                                                             repeatCount="indefinite"></animate>
                                                                </stop>
                                                            </lineargradient>
                                                        </defs>
                                                        <g>
                                                            <polyline stroke="#4fd2dd" stroke-width="3"
                                                                      fill="transparent"
                                                                      points="8, 173.20000000000002 8, 3, 444.20000000000005, 3"></polyline>
                                                            <polyline fill="#235fa7" points="8, 64.95 8, 3, 116.30000000000001, 3
                109.30000000000001, 8 14, 8 14, 57.95
              "></polyline>
                                                            <polyline stroke="#4fd2dd" stroke-width="3"
                                                                      fill="transparent"
                                                                      points="546.5, 3 1090, 3, 1090, 108.25"></polyline>
                                                            <polyline stroke="#235fa7" points="
                568.36, 3 633.9399999999999, 3
                626.9399999999999, 9 575.36, 9
              "></polyline>
                                                            <polyline stroke="#4fd2dd" points="
                983.7, 3 1090, 3 1090, 43.300000000000004
                1084, 36.300000000000004 1084, 9 990.7, 9
              "></polyline>
                                                            <polyline stroke="#235fa7" stroke-width="3"
                                                                      fill="transparent"
                                                                      points="8, 216.5 8, 430 334.9, 430"></polyline>
                                                            <polyline stroke="#4fd2dd" points="
                8, 238.15 8, 303.09999999999997
                2, 296.09999999999997 2, 245.15
              "></polyline>
                                                            <polyline stroke="#235fa7" stroke-width="3"
                                                                      fill="transparent"
                                                                      points="382.54999999999995, 430 1090, 430 1090, 151.54999999999998"></polyline>
                                                            <polyline stroke="#4fd2dd" points="
                1005.5600000000001, 430 1090, 430 1090, 346.40000000000003
                1084, 353.40000000000003 1084, 424 1012.5600000000001, 424
              "></polyline>
                                                        </g>
                                                    </svg>
                                                    <div class="border-box-content">
                                                        <div class="top-data-center__gdX8x">
                                                            <div class="data-center-left__x_3Dn">
                                                                <div class="cus-decoration__xJ1sI"
                                                                     style="min-width: 200px;"><img
                                                                        class="title-bg__y2cmY normal-title-bg__B7kMO"
                                                                        src="/media/css/tv/images/efak07.png">
                                                                    <svg width="200px"
                                                                         height="75px">
                                                                        <defs>
                                                                            <path id="border-box-8-path-7f6b8aa0adfd454eaaee4c269c744d14"
                                                                                  d="M 20 0 L 200, 0 L 200, 45 L 180, 75 L 0, 75 L 0, 30 L 20,0"
                                                                                  fill="transparent"></path>
                                                                            <radialgradient
                                                                                    id="border-box-8-gradient-7f6b8aa0adfd454eaaee4c269c744d14"
                                                                                    cx="50%" cy="50%" r="50%">
                                                                                <stop offset="0%" stop-color="#fff"
                                                                                      stop-opacity="1"></stop>
                                                                                <stop offset="100%" stop-color="#fff"
                                                                                      stop-opacity="0"></stop>
                                                                            </radialgradient>
                                                                            <mask id="border-box-8-mask-7f6b8aa0adfd454eaaee4c269c744d14">
                                                                                <circle cx="0" cy="0" r="150"
                                                                                        fill="url(#border-box-8-gradient-7f6b8aa0adfd454eaaee4c269c744d14)">
                                                                                </circle>
                                                                            </mask>
                                                                        </defs>
                                                                        <line x1="0" y1="15" x2="10" y2="0"
                                                                              stroke="#1a98fc" stroke-width="3"></line>
                                                                        <polygon fill="#2cf7fe" fill-opacity="0.2"
                                                                                 stroke="#1a98fc"
                                                                                 points="0,30 0, 75 180,75 200,45 200,0 20,0"
                                                                                 stroke-width="3"></polygon>
                                                                        <line x1="190" y1="75" x2="200" y2="60"
                                                                              stroke="#1a98fc" stroke-width="3"></line>
                                                                    </svg>
                                                                    <div class="decoration-content__H9c6U">
                                                                        <div class="node-health-block__y9j82">
                                                                            <div class="node-health__KOzGT">CONSUMERS
                                                                            </div>
                                                                            <div class="health-num__VIsIc">
                                                                                <div class="dv-water-pond-level"
                                                                                     style="color: #08ffff">
                                                                                    <text id="efak_dashboard_panel_consumers">
                                                                                        0
                                                                                    </text>
                                                                                </div>
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                                <div class="cus-decoration__xJ1sI decoration-item__DDnwx"
                                                                     style="min-width: 85px;">
                                                                    <img class="title-bg__y2cmY normal-title-bg__B7kMO"
                                                                         src="/media/css/tv/images/efak07.png">
                                                                    <svg width="120px"
                                                                         height="75px">
                                                                        <defs>
                                                                            <path id="border-box-8-path-47850b39fd524f4e8b043d493708329f"
                                                                                  d="M 20 0 L 120, 0 L 120, 45 L 100, 75 L 0, 75 L 0, 30 L 20,0"
                                                                                  fill="transparent"></path>
                                                                            <radialgradient
                                                                                    id="border-box-8-gradient-47850b39fd524f4e8b043d493708329f"
                                                                                    cx="50%" cy="50%" r="50%">
                                                                                <stop offset="0%" stop-color="#fff"
                                                                                      stop-opacity="1"></stop>
                                                                                <stop offset="100%" stop-color="#fff"
                                                                                      stop-opacity="0"></stop>
                                                                            </radialgradient>
                                                                            <mask id="border-box-8-mask-47850b39fd524f4e8b043d493708329f">
                                                                                <circle cx="0" cy="0" r="150"
                                                                                        fill="url(#border-box-8-gradient-47850b39fd524f4e8b043d493708329f)">
                                                                                </circle>
                                                                            </mask>
                                                                        </defs>
                                                                        <line x1="0" y1="15" x2="10" y2="0"
                                                                              stroke="#1a98fc" stroke-width="3"></line>
                                                                        <polygon fill="#2cf7fe" fill-opacity="0.2"
                                                                                 stroke="#1a98fc"
                                                                                 points="0,30 0, 75 100,75 120,45 120,0 20,0"
                                                                                 stroke-width="3"></polygon>
                                                                        <line x1="110" y1="75" x2="120" y2="60"
                                                                              stroke="#1a98fc" stroke-width="3"></line>
                                                                    </svg>
                                                                    <div class="decoration-content__H9c6U">
                                                                        <div class="node-info-block__DQGxr">
                                                                            <div class="node-title__YHkc6">BROKERS</div>
                                                                            <div>
                                                                                <div class="dv-water-pond-level"
                                                                                     style="color: #08ffff">
                                                                                    <text id="efak_dashboard_panel_brokers">
                                                                                        0
                                                                                    </text>
                                                                                </div>
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                                <div class="cus-decoration__xJ1sI decoration-item__DDnwx"
                                                                     style="min-width: 85px;">
                                                                    <img class="title-bg__y2cmY normal-title-bg__B7kMO"
                                                                         src="/media/css/tv/images/efak07.png">
                                                                    <svg width="120px"
                                                                         height="75px">
                                                                        <defs>
                                                                            <path id="border-box-8-path-4ec32b408ece4dc09646b686371003e8"
                                                                                  d="M 20 0 L 120, 0 L 120, 45 L 100, 75 L 0, 75 L 0, 30 L 20,0"
                                                                                  fill="transparent"></path>
                                                                            <radialgradient
                                                                                    id="border-box-8-gradient-4ec32b408ece4dc09646b686371003e8"
                                                                                    cx="50%" cy="50%" r="50%">
                                                                                <stop offset="0%" stop-color="#fff"
                                                                                      stop-opacity="1"></stop>
                                                                                <stop offset="100%" stop-color="#fff"
                                                                                      stop-opacity="0"></stop>
                                                                            </radialgradient>
                                                                            <mask id="border-box-8-mask-4ec32b408ece4dc09646b686371003e8">
                                                                                <circle cx="0" cy="0" r="150"
                                                                                        fill="url(#border-box-8-gradient-4ec32b408ece4dc09646b686371003e8)">
                                                                                </circle>
                                                                            </mask>
                                                                        </defs>
                                                                        <line x1="0" y1="15" x2="10" y2="0"
                                                                              stroke="#1a98fc" stroke-width="3"></line>
                                                                        <polygon fill="#2cf7fe" fill-opacity="0.2"
                                                                                 stroke="#1a98fc"
                                                                                 points="0,30 0, 75 100,75 120,45 120,0 20,0"
                                                                                 stroke-width="3"></polygon>
                                                                        <line x1="110" y1="75" x2="120" y2="60"
                                                                              stroke="#1a98fc" stroke-width="3"></line>
                                                                    </svg>
                                                                    <div class="decoration-content__H9c6U">
                                                                        <div class="node-info-block__DQGxr">
                                                                            <div class="node-title__YHkc6">ZOOKEEPERS
                                                                            </div>
                                                                            <div>
                                                                                <div class="dv-water-pond-level"
                                                                                     style="color: #08ffff">
                                                                                    <text id="efak_dashboard_panel_zookeepers">
                                                                                        0
                                                                                    </text>
                                                                                </div>
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                                <div class="cus-decoration__xJ1sI decoration-item__DDnwx">
                                                                    <img
                                                                            class="title-bg__y2cmY normal-title-bg__B7kMO"
                                                                            src="/media/css/tv/images/efak07.png">
                                                                    <svg width="120px"
                                                                         height="75px">
                                                                        <defs>
                                                                            <path id="border-box-8-path-0465a15f2261406eb6fb04d7e54d8e30"
                                                                                  d="M 20 0 L 120, 0 L 120, 45 L 100, 75 L 0, 75 L 0, 30 L 20,0"
                                                                                  fill="transparent"></path>
                                                                            <radialgradient
                                                                                    id="border-box-8-gradient-0465a15f2261406eb6fb04d7e54d8e30"
                                                                                    cx="50%" cy="50%" r="50%">
                                                                                <stop offset="0%" stop-color="#fff"
                                                                                      stop-opacity="1"></stop>
                                                                                <stop offset="100%" stop-color="#fff"
                                                                                      stop-opacity="0"></stop>
                                                                            </radialgradient>
                                                                            <mask id="border-box-8-mask-0465a15f2261406eb6fb04d7e54d8e30">
                                                                                <circle cx="0" cy="0" r="150"
                                                                                        fill="url(#border-box-8-gradient-0465a15f2261406eb6fb04d7e54d8e30)">
                                                                                </circle>
                                                                            </mask>
                                                                        </defs>
                                                                        <line x1="0" y1="15" x2="10" y2="0"
                                                                              stroke="#1a98fc" stroke-width="3"></line>
                                                                        <polygon fill="#2cf7fe" fill-opacity="0.2"
                                                                                 stroke="#1a98fc"
                                                                                 points="0,30 0, 75 100,75 120,45 120,0 20,0"
                                                                                 stroke-width="3"></polygon>
                                                                        <line x1="110" y1="75" x2="120" y2="60"
                                                                              stroke="#1a98fc" stroke-width="3"></line>
                                                                    </svg>
                                                                    <div class="decoration-content__H9c6U">
                                                                        <div class="node-info-block__DQGxr">
                                                                            <!-- <div class="node-title__YHkc6" style="color: rgb(255, 70, 70);">XXX</div> -->
                                                                            <div class="node-title__YHkc6">TOPICS
                                                                            </div>
                                                                            <div>
                                                                                <div class="dv-water-pond-level"
                                                                                     style="color: #08ffff">
                                                                                    <text id="efak_dashboard_panel_topics">
                                                                                        0
                                                                                    </text>
                                                                                </div>
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                            <div class="data-center-right__EtJo0">
                                                                <div class="waterBlock__sMvLt">
                                                                    <div class="dv-water-pond-level">
                                                                        <svg>
                                                                            <defs>
                                                                                <lineargradient
                                                                                        id="water-level-pond-a1abf9595caf400191a13d533fbb932d"
                                                                                        x1="0%"
                                                                                        y1="0%" x2="0%" y2="100%">
                                                                                    <stop offset="0"
                                                                                          stop-color="#3DE7C9"></stop>
                                                                                    <stop offset="100"
                                                                                          stop-color="#00BAFF"></stop>
                                                                                </lineargradient>
                                                                            </defs>
                                                                            <text id="efak_tv_monitor_cpu_usage"
                                                                                  stroke="url(#water-level-pond-a1abf9595caf400191a13d533fbb932d)"
                                                                                  fill="url(#water-level-pond-a1abf9595caf400191a13d533fbb932d)"
                                                                                  x="40"
                                                                                  y="37.5">0.0%
                                                                            </text>
                                                                            <rect x="2" y="2" rx="10" ry="10" width="76"
                                                                                  height="71"
                                                                                  stroke="url(#water-level-pond-a1abf9595caf400191a13d533fbb932d)"></rect>
                                                                        </svg>
                                                                        <canvas style="border-radius: 10px;" width="64"
                                                                                height="59"></canvas>
                                                                    </div>
                                                                    <div class="water-title__R250P">cpu usage</div>
                                                                </div>
                                                                <div class="waterBlock__sMvLt">
                                                                    <div class="dv-water-pond-level">
                                                                        <svg>
                                                                            <defs>
                                                                                <lineargradient
                                                                                        id="water-level-pond-b9a9b284da694a00a3118e2919f50f4c"
                                                                                        x1="0%"
                                                                                        y1="0%" x2="0%" y2="100%">
                                                                                    <stop offset="0"
                                                                                          stop-color="#3DE7C9"></stop>
                                                                                    <stop offset="100"
                                                                                          stop-color="#00BAFF"></stop>
                                                                                </lineargradient>
                                                                            </defs>
                                                                            <text id="efak_tv_monitor_mem_usage"
                                                                                  stroke="url(#water-level-pond-b9a9b284da694a00a3118e2919f50f4c)"
                                                                                  fill="url(#water-level-pond-b9a9b284da694a00a3118e2919f50f4c)"
                                                                                  x="40"
                                                                                  y="37.5">0.0%
                                                                            </text>
                                                                            <rect x="2" y="2" rx="10" ry="10" width="76"
                                                                                  height="71"
                                                                                  stroke="url(#water-level-pond-b9a9b284da694a00a3118e2919f50f4c)"></rect>
                                                                        </svg>
                                                                        <canvas style="border-radius: 10px;" width="64"
                                                                                height="59"></canvas>
                                                                    </div>
                                                                    <div class="water-title__R250P">memory usage</div>
                                                                </div>
                                                            </div>
                                                        </div>
                                                        <div class="data-center-bottom__LGWBa">
                                                            <div class="node-info-panel__nMMgG">
                                                                <div class="big-node-panel__vS6qC">
                                                                    <div class="node-panel-item__kasVM"
                                                                         style="height: 97%; width: 97%;">
                                                                        <div class="left-panel-item__bgsOB"><img
                                                                                src="/media/css/tv/images/efak02.svg"
                                                                                class="node-img__BKYYg">
                                                                            <div class="cus-decoration__xJ1sI node-host__yfOfA"
                                                                                 style="height: 46px;"><img
                                                                                    class="title-bg__y2cmY normal-title-bg__B7kMO"
                                                                                    src="/media/css/tv/images/efak07.png"><img
                                                                                    class="title-bg__y2cmY reverse-title-bg__OBWRI"
                                                                                    src="/media/css/tv/images/efak07.png">
                                                                                <svg width="263px"
                                                                                     height="46px">
                                                                                    <defs>
                                                                                        <path id="border-box-8-path-0943cbe1074e44b18f162e66d0a6fb8a"
                                                                                              d="M 20 0 L 263, 0 L 263, 16 L 243, 46 L 0, 46 L 0, 30 L 20,0"
                                                                                              fill="transparent"></path>
                                                                                        <radialgradient
                                                                                                id="border-box-8-gradient-0943cbe1074e44b18f162e66d0a6fb8a"
                                                                                                cx="50%"
                                                                                                cy="50%" r="50%">
                                                                                            <stop offset="0%"
                                                                                                  stop-color="#fff"
                                                                                                  stop-opacity="1"></stop>
                                                                                            <stop offset="100%"
                                                                                                  stop-color="#fff"
                                                                                                  stop-opacity="0"></stop>
                                                                                        </radialgradient>
                                                                                        <mask id="border-box-8-mask-0943cbe1074e44b18f162e66d0a6fb8a">
                                                                                            <circle cx="0" cy="0"
                                                                                                    r="150"
                                                                                                    fill="url(#border-box-8-gradient-0943cbe1074e44b18f162e66d0a6fb8a)">
                                                                                            </circle>
                                                                                        </mask>
                                                                                    </defs>
                                                                                    <polygon fill="#2cf7fe"
                                                                                             fill-opacity="0.2"
                                                                                             stroke="#1a98fc"
                                                                                             points="0,30 0, 46 243,46 263,16 263,0 20,0"
                                                                                             stroke-width="3"></polygon>
                                                                                </svg>
                                                                                <div class="decoration-content__H9c6U"><span
                                                                                        style="font-size: 16px;">TOPIC CAPACITY RANK</span>
                                                                                </div>
                                                                            </div>
                                                                            <div id="efak_dashboard_capacity_table"
                                                                                 class="service-info__OzcCc">
                                                                            </div>
                                                                        </div>
                                                                        <div class="right-panel-item__TEENy">
                                                                            <div class="machine-info-item__pMWYQ">
                                                                                <div class="info-title__CXw5K"
                                                                                     style="font-size: 18px;">CLUSTER
                                                                                </div>
                                                                                <div id="efak_tv_cluster"
                                                                                     class="info-value__yPbV5"
                                                                                     style="font-size: 18px;">
                                                                                    -
                                                                                </div>
                                                                            </div>
                                                                            <div class="machine-info-item__pMWYQ">
                                                                                <div class="info-title__CXw5K"
                                                                                     style="font-size: 18px;">EFAK
                                                                                    VERSION
                                                                                </div>
                                                                                <div id="efak_tv_version"
                                                                                     class="info-value__yPbV5"
                                                                                     style="font-size: 18px;">-
                                                                                </div>
                                                                            </div>
                                                                            <div class="machine-info-item__pMWYQ">
                                                                                <div class="info-title__CXw5K"
                                                                                     style="font-size: 18px;">CAPACITY
                                                                                </div>
                                                                                <div id="efak_tv_capacity"
                                                                                     class="info-value__yPbV5"
                                                                                     style="font-size: 18px;">-
                                                                                </div>
                                                                            </div>
                                                                            <div class="machine-info-item__pMWYQ">
                                                                                <div class="info-title__CXw5K"
                                                                                     style="font-size: 18px;">MODE
                                                                                </div>
                                                                                <div id="efak_tv_mode"
                                                                                     class="info-value__yPbV5"
                                                                                     style="font-size: 18px;">-
                                                                                </div>
                                                                            </div>
                                                                            <div class="machine-info-item__pMWYQ">
                                                                                <div class="info-title__CXw5K"
                                                                                     style="font-size: 18px;">WORKNODE
                                                                                </div>
                                                                                <div id="efak_tv_worknode"
                                                                                     class="info-value__yPbV5"
                                                                                     style="font-size: 18px;">-
                                                                                </div>
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                            <div class="dv-scroll-ranking-board__wHQDU">
                                                                <div class="alert-panel__taEuU">
                                                                    <div class="title__hGFf9">KAFAK PRODUCER SERVICES
                                                                    </div>
                                                                    <div class="empty-content__aqiEm"><img
                                                                            src="/media/css/tv/images/efak03.png">
                                                                        <div id="efak_tv_app" class="no-data__XgHjd"
                                                                             style="font-size: 25px; font-weight: bold;">
                                                                            0
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="bottom-center__RE8vV">
                                                    <div class="dv-border-box-11" style="height: 354.15px;">
                                                        <svg class="dv-border-svg-container"
                                                             width="1093" height="354">
                                                            <defs>
                                                                <filter id="border-box-11-filterId-d4d7ef695d044d25902053cb79b3a5bf"
                                                                        height="150%"
                                                                        width="150%" x="-25%" y="-25%">
                                                                    <femorphology operator="dilate" radius="2"
                                                                                  in="SourceAlpha" result="thicken">
                                                                    </femorphology>
                                                                    <fegaussianblur in="thicken" stdDeviation="3"
                                                                                    result="blurred"></fegaussianblur>
                                                                    <feflood flood-color="#235fa7"
                                                                             result="glowColor"></feflood>
                                                                    <fecomposite in="glowColor" in2="blurred"
                                                                                 operator="in" result="softGlowColored">
                                                                    </fecomposite>
                                                                    <femerge>
                                                                        <femergenode in="softGlowColored"></femergenode>
                                                                        <femergenode in="SourceGraphic"></femergenode>
                                                                    </femerge>
                                                                </filter>
                                                            </defs>
                                                            <polygon fill="transparent" points="
          20, 32 421.5, 32 441.5, 53
          651.5, 53 671.5, 32
          1073, 32 1085, 48 1085, 329 1073, 346
          20, 346 8, 329 8, 50
        "></polygon>
                                                            <polyline stroke="#4fd2dd"
                                                                      filter="url(#border-box-11-filterId-d4d7ef695d044d25902053cb79b3a5bf)"
                                                                      points="
          421.5, 30
          20, 30 7, 50 7, 143.5
          13, 148.5 13, 228.5
          7, 233.5 7, 327
          20, 347 1073, 347 1086, 327
          1086, 233.5 1080, 228.5
          1080, 148.5 1086, 143.5
          1086, 50 1073, 30 671.5, 30
          651.5, 7 441.5, 7
          421.5, 30 441.5, 52
          651.5, 52 671.5, 30
        "></polyline>
                                                            <polygon stroke="#4fd2dd" fill="transparent" points="
          666.5, 30 650.5, 11
          644.5, 11 663.5, 34
        "></polygon>
                                                            <polygon stroke="#4fd2dd" fill="transparent" points="
          426.5, 30 443.5, 49
          449.5, 49 429.5, 26
        "></polygon>
                                                            <polygon stroke="#4fd2dd" fill="rgba(35,95,167,0.3)"
                                                                     filter="url(#border-box-11-filterId-d4d7ef695d044d25902053cb79b3a5bf)"
                                                                     points="
          660.5, 37 639.5, 11
          444.5, 11 432.5, 23
          454.5, 49 649.5, 49
        "></polygon>
                                                            <polygon
                                                                    filter="url(#border-box-11-filterId-d4d7ef695d044d25902053cb79b3a5bf)"
                                                                    fill="#4fd2dd" opacity="1" points="
          411.5, 37 390.5, 37
          396.5, 46 417.5, 46
        "></polygon>
                                                            <polygon
                                                                    filter="url(#border-box-11-filterId-d4d7ef695d044d25902053cb79b3a5bf)"
                                                                    fill="#4fd2dd" opacity="0.7" points="
          381.5, 37 360.5, 37
          366.5, 46 387.5, 46
        "></polygon>
                                                            <polygon
                                                                    filter="url(#border-box-11-filterId-d4d7ef695d044d25902053cb79b3a5bf)"
                                                                    fill="#4fd2dd" opacity="0.5" points="
          351.5, 37 330.5, 37
          336.5, 46 357.5, 46
        "></polygon>
                                                            <polygon
                                                                    filter="url(#border-box-11-filterId-d4d7ef695d044d25902053cb79b3a5bf)"
                                                                    fill="#4fd2dd" opacity="1" points="
          701.5, 37 680.5, 37
          674.5, 46 695.5, 46
        "></polygon>
                                                            <polygon
                                                                    filter="url(#border-box-11-filterId-d4d7ef695d044d25902053cb79b3a5bf)"
                                                                    fill="#4fd2dd" opacity="0.7" points="
          731.5, 37 710.5, 37
          704.5, 46 725.5, 46
        "></polygon>
                                                            <polygon
                                                                    filter="url(#border-box-11-filterId-d4d7ef695d044d25902053cb79b3a5bf)"
                                                                    fill="#4fd2dd" opacity="0.5" points="
          761.5, 37 740.5, 37
          734.5, 46 755.5, 46
        "></polygon>
                                                            <text class="dv-border-box-11-title" x="546.5" y="32"
                                                                  fill="#fff" font-size="18"
                                                                  text-anchor="middle" dominant-baseline="middle">
                                                                PRODUCER METRICS
                                                            </text>
                                                            <!-- lastest 7 days producer messages -->
                                                            <polygon fill="#4fd2dd"
                                                                     filter="url(#border-box-11-filterId-d4d7ef695d044d25902053cb79b3a5bf)"
                                                                     points="
          7, 146.5 11, 150.5
          11, 226.5 7, 230.5
        "></polygon>
                                                            <polygon fill="#4fd2dd"
                                                                     filter="url(#border-box-11-filterId-d4d7ef695d044d25902053cb79b3a5bf)"
                                                                     points="
          1086, 146.5 1082, 150.5
          1082, 226.5 1086, 230.5
        "></polygon>
                                                        </svg>
                                                        <div class="border-box-content">
                                                            <div class="row" style="margin-top: 5%">
                                                                <div id="efak_dashboard_logsize_chart"
                                                                     class=""></div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="ant-col ant-col-5" style="padding-left: 12px; padding-right: 12px;">
                                            <div class="center__Q6gqV" style="height: 787px;">
                                                <div class="dv-border-box-1 node-metrics-top__LjyLl">
                                                    <svg class="border" width="375"
                                                         height="169">
                                                        <polygon fill="transparent" points="10, 27 10, 142 13, 145 13, 148 24, 158
        38, 158 41, 161 73, 161 75, 159 81, 159
        85, 163 290, 163 294, 159 300, 159
        302, 161 334, 161 337, 158
        351, 158 362, 148 362, 145
        365, 142 365, 27 362, 25 362, 21
        351, 11 337, 11 334, 8 302, 8 300, 10
        294, 10 290, 6 85, 6 81, 10 75, 10 73, 8 41, 8 38, 11 24, 11 13, 21 13, 24"></polygon>
                                                    </svg>
                                                    <svg width="150px" height="150px" class="left-top border">
                                                        <polygon fill="#4fd2dd"
                                                                 points="6,66 6,18 12,12 18,12 24,6 27,6 30,9 36,9 39,6 84,6 81,9 75,9 73.2,7 40.8,7 37.8,10.2 24,10.2 12,21 12,24 9,27 9,51 7.8,54 7.8,63">
                                                        </polygon>
                                                        <polygon fill="#235fa7"
                                                                 points="27.599999999999998,4.8 38.4,4.8 35.4,7.8 30.599999999999998,7.8"></polygon>
                                                        <polygon fill="#4fd2dd"
                                                                 points="9,54 9,63 7.199999999999999,66 7.199999999999999,75 7.8,78 7.8,110 8.4,110 8.4,66 9.6,66 9.6,54">
                                                        </polygon>
                                                    </svg>
                                                    <svg width="150px" height="150px" class="right-top border">
                                                        <polygon fill="#4fd2dd"
                                                                 points="6,66 6,18 12,12 18,12 24,6 27,6 30,9 36,9 39,6 84,6 81,9 75,9 73.2,7 40.8,7 37.8,10.2 24,10.2 12,21 12,24 9,27 9,51 7.8,54 7.8,63">
                                                        </polygon>
                                                        <polygon fill="#235fa7"
                                                                 points="27.599999999999998,4.8 38.4,4.8 35.4,7.8 30.599999999999998,7.8"></polygon>
                                                        <polygon fill="#4fd2dd"
                                                                 points="9,54 9,63 7.199999999999999,66 7.199999999999999,75 7.8,78 7.8,110 8.4,110 8.4,66 9.6,66 9.6,54">
                                                        </polygon>
                                                    </svg>
                                                    <svg width="150px" height="150px" class="left-bottom border">
                                                        <polygon fill="#4fd2dd"
                                                                 points="6,66 6,18 12,12 18,12 24,6 27,6 30,9 36,9 39,6 84,6 81,9 75,9 73.2,7 40.8,7 37.8,10.2 24,10.2 12,21 12,24 9,27 9,51 7.8,54 7.8,63">
                                                        </polygon>
                                                        <polygon fill="#235fa7"
                                                                 points="27.599999999999998,4.8 38.4,4.8 35.4,7.8 30.599999999999998,7.8"></polygon>
                                                        <polygon fill="#4fd2dd"
                                                                 points="9,54 9,63 7.199999999999999,66 7.199999999999999,75 7.8,78 7.8,110 8.4,110 8.4,66 9.6,66 9.6,54">
                                                        </polygon>
                                                    </svg>
                                                    <svg width="150px" height="150px" class="right-bottom border">
                                                        <polygon fill="#4fd2dd"
                                                                 points="6,66 6,18 12,12 18,12 24,6 27,6 30,9 36,9 39,6 84,6 81,9 75,9 73.2,7 40.8,7 37.8,10.2 24,10.2 12,21 12,24 9,27 9,51 7.8,54 7.8,63">
                                                        </polygon>
                                                        <polygon fill="#235fa7"
                                                                 points="27.599999999999998,4.8 38.4,4.8 35.4,7.8 30.599999999999998,7.8"></polygon>
                                                        <polygon fill="#4fd2dd"
                                                                 points="9,54 9,63 7.199999999999999,66 7.199999999999999,75 7.8,78 7.8,110 8.4,110 8.4,66 9.6,66 9.6,54">
                                                        </polygon>
                                                    </svg>
                                                    <div class="border-box-content">
                                                        <div class="chart-title__nQpiI">cpu used</div>
                                                        <div id="efak_dashboard_cpu_used_lastest"
                                                             class="chart-title__nQpiI" style="margin-top: 5%">[ 0% ]
                                                        </div>
                                                        <div class="screen-chart__nZcqk"
                                                             id="efak_dashboard_cpu_used_chart"
                                                             style="position:relative;width: 80%;margin-left:10%">
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="dv-border-box-1 node-metrics-top__LjyLl">
                                                    <svg class="border" width="375"
                                                         height="169">
                                                        <polygon fill="transparent" points="10, 27 10, 142 13, 145 13, 148 24, 158
        38, 158 41, 161 73, 161 75, 159 81, 159
        85, 163 290, 163 294, 159 300, 159
        302, 161 334, 161 337, 158
        351, 158 362, 148 362, 145
        365, 142 365, 27 362, 25 362, 21
        351, 11 337, 11 334, 8 302, 8 300, 10
        294, 10 290, 6 85, 6 81, 10 75, 10 73, 8 41, 8 38, 11 24, 11 13, 21 13, 24"></polygon>
                                                    </svg>
                                                    <svg width="150px" height="150px" class="left-top border">
                                                        <polygon fill="#4fd2dd"
                                                                 points="6,66 6,18 12,12 18,12 24,6 27,6 30,9 36,9 39,6 84,6 81,9 75,9 73.2,7 40.8,7 37.8,10.2 24,10.2 12,21 12,24 9,27 9,51 7.8,54 7.8,63">
                                                        </polygon>
                                                        <polygon fill="#235fa7"
                                                                 points="27.599999999999998,4.8 38.4,4.8 35.4,7.8 30.599999999999998,7.8"></polygon>
                                                        <polygon fill="#4fd2dd"
                                                                 points="9,54 9,63 7.199999999999999,66 7.199999999999999,75 7.8,78 7.8,110 8.4,110 8.4,66 9.6,66 9.6,54">
                                                        </polygon>
                                                    </svg>
                                                    <svg width="150px" height="150px" class="right-top border">
                                                        <polygon fill="#4fd2dd"
                                                                 points="6,66 6,18 12,12 18,12 24,6 27,6 30,9 36,9 39,6 84,6 81,9 75,9 73.2,7 40.8,7 37.8,10.2 24,10.2 12,21 12,24 9,27 9,51 7.8,54 7.8,63">
                                                        </polygon>
                                                        <polygon fill="#235fa7"
                                                                 points="27.599999999999998,4.8 38.4,4.8 35.4,7.8 30.599999999999998,7.8"></polygon>
                                                        <polygon fill="#4fd2dd"
                                                                 points="9,54 9,63 7.199999999999999,66 7.199999999999999,75 7.8,78 7.8,110 8.4,110 8.4,66 9.6,66 9.6,54">
                                                        </polygon>
                                                    </svg>
                                                    <svg width="150px" height="150px" class="left-bottom border">
                                                        <polygon fill="#4fd2dd"
                                                                 points="6,66 6,18 12,12 18,12 24,6 27,6 30,9 36,9 39,6 84,6 81,9 75,9 73.2,7 40.8,7 37.8,10.2 24,10.2 12,21 12,24 9,27 9,51 7.8,54 7.8,63">
                                                        </polygon>
                                                        <polygon fill="#235fa7"
                                                                 points="27.599999999999998,4.8 38.4,4.8 35.4,7.8 30.599999999999998,7.8"></polygon>
                                                        <polygon fill="#4fd2dd"
                                                                 points="9,54 9,63 7.199999999999999,66 7.199999999999999,75 7.8,78 7.8,110 8.4,110 8.4,66 9.6,66 9.6,54">
                                                        </polygon>
                                                    </svg>
                                                    <svg width="150px" height="150px" class="right-bottom border">
                                                        <polygon fill="#4fd2dd"
                                                                 points="6,66 6,18 12,12 18,12 24,6 27,6 30,9 36,9 39,6 84,6 81,9 75,9 73.2,7 40.8,7 37.8,10.2 24,10.2 12,21 12,24 9,27 9,51 7.8,54 7.8,63">
                                                        </polygon>
                                                        <polygon fill="#235fa7"
                                                                 points="27.599999999999998,4.8 38.4,4.8 35.4,7.8 30.599999999999998,7.8"></polygon>
                                                        <polygon fill="#4fd2dd"
                                                                 points="9,54 9,63 7.199999999999999,66 7.199999999999999,75 7.8,78 7.8,110 8.4,110 8.4,66 9.6,66 9.6,54">
                                                        </polygon>
                                                    </svg>
                                                    <div class="border-box-content">
                                                        <div class="chart-title__nQpiI">memory used</div>
                                                        <div id="efak_dashboard_osfreememory_lastest"
                                                             class="chart-title__nQpiI" style="margin-top: 5%">[ 0
                                                            (GB/min)]
                                                        </div>
                                                        <div class="screen-chart__nZcqk"
                                                             id="efak_dashboard_osfree_memory_chart"
                                                             style="position:relative;width: 80%;margin-left:10%">
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="dv-border-box-1 node-metrics-top__LjyLl">
                                                    <svg class="border" width="375"
                                                         height="169">
                                                        <polygon fill="transparent" points="10, 27 10, 142 13, 145 13, 148 24, 158
        38, 158 41, 161 73, 161 75, 159 81, 159
        85, 163 290, 163 294, 159 300, 159
        302, 161 334, 161 337, 158
        351, 158 362, 148 362, 145
        365, 142 365, 27 362, 25 362, 21
        351, 11 337, 11 334, 8 302, 8 300, 10
        294, 10 290, 6 85, 6 81, 10 75, 10 73, 8 41, 8 38, 11 24, 11 13, 21 13, 24"></polygon>
                                                    </svg>
                                                    <svg width="150px" height="150px" class="left-top border">
                                                        <polygon fill="#4fd2dd"
                                                                 points="6,66 6,18 12,12 18,12 24,6 27,6 30,9 36,9 39,6 84,6 81,9 75,9 73.2,7 40.8,7 37.8,10.2 24,10.2 12,21 12,24 9,27 9,51 7.8,54 7.8,63">
                                                        </polygon>
                                                        <polygon fill="#235fa7"
                                                                 points="27.599999999999998,4.8 38.4,4.8 35.4,7.8 30.599999999999998,7.8"></polygon>
                                                        <polygon fill="#4fd2dd"
                                                                 points="9,54 9,63 7.199999999999999,66 7.199999999999999,75 7.8,78 7.8,110 8.4,110 8.4,66 9.6,66 9.6,54">
                                                        </polygon>
                                                    </svg>
                                                    <svg width="150px" height="150px" class="right-top border">
                                                        <polygon fill="#4fd2dd"
                                                                 points="6,66 6,18 12,12 18,12 24,6 27,6 30,9 36,9 39,6 84,6 81,9 75,9 73.2,7 40.8,7 37.8,10.2 24,10.2 12,21 12,24 9,27 9,51 7.8,54 7.8,63">
                                                        </polygon>
                                                        <polygon fill="#235fa7"
                                                                 points="27.599999999999998,4.8 38.4,4.8 35.4,7.8 30.599999999999998,7.8"></polygon>
                                                        <polygon fill="#4fd2dd"
                                                                 points="9,54 9,63 7.199999999999999,66 7.199999999999999,75 7.8,78 7.8,110 8.4,110 8.4,66 9.6,66 9.6,54">
                                                        </polygon>
                                                    </svg>
                                                    <svg width="150px" height="150px" class="left-bottom border">
                                                        <polygon fill="#4fd2dd"
                                                                 points="6,66 6,18 12,12 18,12 24,6 27,6 30,9 36,9 39,6 84,6 81,9 75,9 73.2,7 40.8,7 37.8,10.2 24,10.2 12,21 12,24 9,27 9,51 7.8,54 7.8,63">
                                                        </polygon>
                                                        <polygon fill="#235fa7"
                                                                 points="27.599999999999998,4.8 38.4,4.8 35.4,7.8 30.599999999999998,7.8"></polygon>
                                                        <polygon fill="#4fd2dd"
                                                                 points="9,54 9,63 7.199999999999999,66 7.199999999999999,75 7.8,78 7.8,110 8.4,110 8.4,66 9.6,66 9.6,54">
                                                        </polygon>
                                                    </svg>
                                                    <svg width="150px" height="150px" class="right-bottom border">
                                                        <polygon fill="#4fd2dd"
                                                                 points="6,66 6,18 12,12 18,12 24,6 27,6 30,9 36,9 39,6 84,6 81,9 75,9 73.2,7 40.8,7 37.8,10.2 24,10.2 12,21 12,24 9,27 9,51 7.8,54 7.8,63">
                                                        </polygon>
                                                        <polygon fill="#235fa7"
                                                                 points="27.599999999999998,4.8 38.4,4.8 35.4,7.8 30.599999999999998,7.8"></polygon>
                                                        <polygon fill="#4fd2dd"
                                                                 points="9,54 9,63 7.199999999999999,66 7.199999999999999,75 7.8,78 7.8,110 8.4,110 8.4,66 9.6,66 9.6,54">
                                                        </polygon>
                                                    </svg>
                                                    <div class="border-box-content">
                                                        <div class="chart-title__nQpiI">message in</div>
                                                        <div id="efak_dashboard_message_in_lastest"
                                                             class="chart-title__nQpiI" style="margin-top: 5%">[ 0
                                                            (MSG/min) ]
                                                        </div>
                                                        <div class="screen-chart__nZcqk"
                                                             id="efak_dashboard_msg_in_chart"
                                                             style="position:relative;width: 80%;margin-left:10%">
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="dv-border-box-8 node-metrics-bottom__f2yJf"
                                                     style="height: 314.8px;">
                                                    <svg
                                                            class="dv-border-svg-container" width="375"
                                                            height="315">
                                                        <path stroke="#4fd2dd"
                                                              d="M2.5, 2.5 L372.5, 2.5 L372.5, 312.5 L2.5, 312.5 L2.5, 2.5"
                                                              stroke-width="3" fill="transparent"></path>
                                                    </svg>
                                                    <div class="border-box-content">
                                                        <div class="disk-block__l_9zd">
                                                            <div class="disk-chart-block__d1cO_">
                                                                <div class="disk-chart-title__YGis2">byte in rate
                                                                </div>
                                                                <div id="efak_dashboard_byte_in_lastest"
                                                                     class="chart-title__nQpiI" style="margin-top: 7%">
                                                                    [
                                                                    0
                                                                    (B/sec) ]
                                                                </div>
                                                                <div id="efak_dashboard_byte_in_chart"
                                                                     class="screen-chart__nZcqk"
                                                                     style="width: 80%"></div>
                                                            </div>
                                                            <div class="disk-chart-block__d1cO_">
                                                                <div class="disk-chart-title__YGis2">byte out rate
                                                                </div>
                                                                <div id="efak_dashboard_byte_out_lastest"
                                                                     class="chart-title__nQpiI" style="margin-top: 7%">
                                                                    [
                                                                    0
                                                                    (B/sec) ]
                                                                </div>
                                                                <div id="efak_dashboard_byte_out_chart"
                                                                     class="screen-chart__nZcqk"
                                                                     style="width: 80%"></div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </main>
                        </section>
                    </section>
                </section>
            </section>
        </section>
    </section>
</div>
</body>
<jsp:include page="../public/pro/script.jsp">
    <jsp:param value="plugins/chartjs/Chart.min.js" name="loader"/>
    <jsp:param value="plugins/chartjs/Chart.extension.js" name="loader"/>
    <jsp:param value="plugins/apexcharts-bundle/apexcharts.min.js" name="loader"/>
    <jsp:param value="main/tv/tv.js?v=3.0.0" name="loader"/>
</jsp:include>
</html>
