/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeCapabilities;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.junit.Assert;
import org.junit.Test;

/**
 * XML foldings services tests
 *
 */
public class XMLFoldingsTest {

	private static class ExpectedIndentRange {

		public final int startLine;
		public final int endLine;
		public final String kind;

		public ExpectedIndentRange(int startLine, int endLine, String kind) {
			this.startLine = startLine;
			this.endLine = endLine;
			this.kind = kind;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + endLine;
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + startLine;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExpectedIndentRange other = (ExpectedIndentRange) obj;
			if (endLine != other.endLine)
				return false;
			if (kind == null) {
				if (other.kind != null)
					return false;
			} else if (!kind.equals(other.kind))
				return false;
			if (startLine != other.startLine)
				return false;
			return true;
		}
	}
	
	@Test
	public void testFoldOneLevel () {
		String[] input = new String[] {
			/*0*/"<html>",
			/*1*/"Hello",
			/*2*/"</html>"
		};
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 1)});
	}


	@Test
	public void	testFoldTwoLevel()  {
		String[] input = new String[] {
			/*0*/"<html>",
			/*1*/"<head>",
			/*2*/"Hello",
			/*3*/"</head>",
			/*4*/"</html>"
		};
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 3), r(1, 2)});
	}

	@Test
	public void	testFoldSiblings()  {
		String[] input = new String[] {
			/*0*/"<html>",
			/*1*/"<head>",
			/*2*/"Head",
			/*3*/"</head>",
			/*4*/"<body class=\"f\">",
			/*5*/"Body",
			/*6*/"</body>",
			/*7*/"</html>"
		};
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 6), r(1, 2), r(4, 5)});
	}

	
	@Test
	public void	testFoldComment()  {
		String[] input = new String[] {
			/*0*/"<!--",
			/*1*/" multi line",
			/*2*/"-->",
			/*3*/"<!-- some stuff",
			/*4*/" some more stuff -->",
		};
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 2, "comment"), r(3, 4, "comment")});
	}

	@Test
	public void	testFoldRegions()  {
		String[] input = new String[] {
			/*0*/"<!-- #region -->",
			/*1*/"<!-- #region -->",
			/*2*/"<!-- #endregion -->",
			/*3*/"<!-- #endregion -->",
		};
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 3, "region"), r(1, 2, "region")});
	}

	@Test
	public void	testFoldIncomplete()  {
		String[] input = new String[] {
			/*0*/"<body>",
			/*1*/"<div></div>",
			/*2*/"Hello",
			/*3*/"</div>",
			/*4*/"</body>",
		};
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 3)});
	}

	@Test
	public void	testFoldIcomplete2()  {
		String[] input = new String[] {
			/*0*/"<be><div>",
			/*1*/"<!-- #endregion -->",
			/*2*/"</div>",
		};
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 1)});
	}

	@Test
	public void	testFoldIntersectingRegion()  {
		String[] input = new String[] {
			/*0*/"<body>",
			/*1*/"<!-- #region -->",
			/*2*/"Hello",
			/*3*/"<div></div>",
			/*4*/"</body>",
			/*5*/"<!-- #endregion -->",
		};
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 3)});
	}

	@Test
	public void	testFoldIntersectingRegion2()  {
		String[] input = new String[] {
			/*0*/"<!-- #region -->",
			/*1*/"<body>",
			/*2*/"Hello",
			/*3*/"<!-- #endregion -->",
			/*4*/"<div></div>",
			/*5*/"</body>",
		};
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 3, "region")});
	}

	@Test
	public void	testLimit()  {
		String[] input = new String[] {
			/* 0*/"<div>",
			/* 1*/" <span>",
			/* 2*/"  <b>",
			/* 3*/"  ",
			/* 4*/"  </b>,",
			/* 5*/"  <b>",
			/* 6*/"   <pre>",
			/* 7*/"  ",
			/* 8*/"   </pre>,",
			/* 9*/"   <pre>",
			/*10*/"  ",
			/*11*/"   </pre>,",
			/*12*/"  </b>,",
			/*13*/"  <b>",
			/*14*/"  ",
			/*15*/"  </b>,",
			/*16*/"  <b>",
			/*17*/"  ",
			/*18*/"  </b>",
			/*19*/" </span>",
			/*20*/"</div>",
		};
		assertRanges(input, new ExpectedIndentRange[] {r(0, 19), r(1, 18), r(2, 3), r(5, 11), r(6, 7), r(9, 10), r(13, 14), r(16, 17)}, "no limit", null);
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 19), r(1, 18), r(2, 3), r(5, 11), r(6, 7), r(9, 10), r(13, 14), r(16, 17)}, "limit 8", 8);
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 19), r(1, 18), r(2, 3), r(5, 11), r(6, 7), r(13, 14), r(16, 17)}, "limit 7", 7);
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 19), r(1, 18), r(2, 3), r(5, 11), r(13, 14), r(16, 17)}, "limit 6", 6);
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 19), r(1, 18), r(2, 3), r(5, 11), r(13, 14)}, "limit 5", 5);
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 19), r(1, 18), r(2, 3), r(5, 11)}, "limit 4", 4);
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 19), r(1, 18), r(2, 3)}, "limit 3", 3);
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 19), r(1, 18)}, "limit 2", 2);
		assertRanges(input,  new ExpectedIndentRange[] {r(0, 19)}, "limit 1", 1);
	}
			
	private static void assertRanges(String[] lines, ExpectedIndentRange[] expected) {
		assertRanges(lines, expected, "", null);
	}

	private static void assertRanges(String[] lines, ExpectedIndentRange[] expected, String message, Integer nRanges) {
		TextDocument document = new TextDocument(String.join("\n", lines), "test://foo/bar.json");

		XMLLanguageService languageService = new XMLLanguageService();

		FoldingRangeCapabilities context = new FoldingRangeCapabilities();
		context.setRangeLimit(nRanges);
		List<FoldingRange> actual = languageService.getFoldingRanges(document, context);

		List<ExpectedIndentRange> actualRanges = new ArrayList<>();
		for (FoldingRange f : actual) {
			actualRanges.add(r(f.getStartLine(), f.getEndLine(), f.getKind()));
		}
		Collections.sort(actualRanges, (r1, r2) -> r1.startLine - r2.startLine);
		Assert.assertArrayEquals(message, expected, actualRanges.toArray());
	}

	private static ExpectedIndentRange r(int startLine, int endLine) {
		return r(startLine, endLine, null);
	}

	private static ExpectedIndentRange r(int startLine, int endLine, String kind) {
		return new ExpectedIndentRange(startLine, endLine, kind);
	}
}
