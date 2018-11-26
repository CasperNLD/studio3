package com.aptana.js.core.parsing;

import com.aptana.parsing.IParser;

public class GraalJSParserTest extends JSParserTest
{

	@Override
	protected IParser createParser()
	{
		return new GraalJSParser();
	}

	@Override
	protected String mismatchedToken(int line, int offset, String token)
	{
		return "SyntaxError:" + line + ":" + offset + " Expected an operand but found " + token;
	}

	@Override
	protected String unexpectedToken(String token)
	{
		return token;
	}

}
