package com.letrungthang.blogspot.main;

import java.util.List;
import java.util.Scanner;
import java.util.Stack;

/**
 * @author ThangLe
 * @version 10 April 2016
 *
 */
public class Spreadsheet {

	private CellSet vistedCells = new CellSet();
	private static List<List<String>> CellMatrix;

	Spreadsheet() {
		CellMatrix = Global.gCellMatrix;
	}

	/**
	 * To detect whether expression of a cell is cyclic dependencies or not.
	 * 
	 * @return true: cell is cyclic dependency. false: cell is no cyclic
	 *         dependency.
	 */
	public boolean detectCyclicDependency(Cell cell) {
		boolean ret = false;

		if (!Utility.isReferencedCell(cell.expression)) {
			// no harm even if the cell not exist on the Set
			vistedCells.remove(cell);
			// A non-reference cell is a cell having no reference to other cells
			// to get its value
			return false;
		} else {

			if (vistedCells.contains(cell)) {
				return true; // cyclic reference
			} else {
				// add the cell visited to a set for tracking back
				vistedCells.add(cell);

				// split the expression
				String exprSplit[] = cell.expression.split("\\s+");
				for (int i = 0; i < exprSplit.length; i++) {

					String character = exprSplit[i];
					// find operand of expression that references to other cell
					if (Character.isLetter(character.charAt(0))) {
						Cell cellBld = Utility.buildCell(character.substring(0, 1), character.substring(1));
						// recursive to visit all dependencies of the current
						// cell
						ret = detectCyclicDependency(cellBld);
						// detect cyclic
						if (ret == true) {
							break; // a cell is cyclic dependency
						} else {
							vistedCells.remove(cellBld);
						}
					}
				}
			}

		}

		return ret;
	}

	/**
	 * This method tests to see whether the value of a String is a legal RPN
	 * mathematical operator or not.
	 * 
	 * @param next
	 *            is the value to be tested
	 * @return whether the next value is a mathematical operator or not
	 */
	public boolean nextIsOperator(String next) {
		return (next.equals("+") || next.equals("-") || next.equals("*") || next.equals("/"));
	}

	/**
	 * check if an operand links to an other cell
	 * 
	 */
	public boolean nextIsReferencedCell(String next) {
		if (Character.isLetter(next.charAt(0))) {
			return true;
		}

		return false;
	}

	/**
	 * Reverse Polish Notation algorithm
	 * 
	 * @param cell
	 */
	public double RPNAlgorithm(Cell cell) {

		// scanner to manipulate input and stack to store double values
		String next;
		Stack<Double> stack = new Stack<Double>();
		Scanner scan = new Scanner(cell.expression.trim());

		// loop while there are tokens left in scan
		while (scan.hasNext()) {
			// retrieve the next token from the input
			next = scan.next();

			// see if token is mathematical operator
			if (nextIsOperator(next)) {
				// ensure there are enough numbers on stack
				if (stack.size() > 1) {
					if (next.equals("+")) {
						stack.push((Double) stack.pop() + (Double) stack.pop());
					} else if (next.equals("-")) {
						stack.push(-(Double) stack.pop() + (Double) stack.pop());
					} else if (next.equals("*")) {
						stack.push((Double) stack.pop() * (Double) stack.pop());
					} else if (next.equals("/")) {
						double first = stack.pop();
						double second = stack.pop();

						if (first == 0) {
							System.out.println("The RPN equation attempted to divide by zero.");
						} else {
							stack.push(second / first);
						}
					}
				} else {
					System.out.println(
							"A mathematical operator occured before there were enough numerical values for it to evaluate.");
				}
			} else {
				try {
					if (nextIsReferencedCell(next)) {
						Cell ce = Utility.buildCell(next.substring(0, 1), next.substring(1));
						// recursive here to calculate operand that referencing
						// to other cell
						stack.push(RPNAlgorithm(ce));
					} else {
						stack.push(Double.parseDouble(next));
					}
				} catch (NumberFormatException c) {
					System.out.println("The string is not a valid RPN equation.");
				}
			}
		}

		if (stack.size() > 1) {
			System.out.println(
					"There too many numbers and not enough mathematical operators with which to evaluate them.");
		}

		return (Double) stack.pop();
	}

	/*
	 * entry function
	 */
	public static void main(String[] args) {
		// 1. Initialize Spreadsheet
		Spreadsheet spr = new Spreadsheet();
		// 2. read csv and convert to a matrix
		Utility.CSVReader(CellMatrix);
		// 3. detect cyclic dependency
		Cell ce = Utility.buildCell("C", "1");
		boolean ret = spr.detectCyclicDependency(ce);
		System.out.println("res =" + ret);
		// 4. calculate RPN
		if (!ret) {
			double rpnRes = 0;
			rpnRes = spr.RPNAlgorithm(ce);

			System.out.println("rpn is =" + String.format("%.5f", rpnRes));
		}
	}

}
