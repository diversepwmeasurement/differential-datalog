/*
 * Copyright (c) 2021 VMware, Inc.
 * SPDX-License-Identifier: MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.dbsp.circuits.operators;

import org.dbsp.algebraic.dynamicTyping.types.Type;
import org.dbsp.circuits.Scheduler;

import java.util.function.Function;

/**
 * A unary operator that applies a specified function to its input.
 */
public class FunctionOperator extends UnaryOperator {
    private final Function<Object, Object> computation;
    final String name;

    /**
     * Create a unary operator that computes by applying a function to its input.
     * @param name           Operator name.
     * @param inputType      Input type.
     * @param outputType     Output type.
     * @param computation    Function that is applied to input to produce the output.
     */
    public FunctionOperator(String name, Type inputType, Type outputType, Function<Object, Object> computation) {
        super(inputType, outputType);
        this.computation = computation;
        this.name = name;
    }

    @Override
    public Object evaluate(Object input, Scheduler scheduler) {
        return this.computation.apply(input);
    }

    public String toString() {
        return this.name;
    }
}
