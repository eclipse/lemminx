<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<html>
			<body>
				<h2>Space Checking</h2>
				<p>
					<xsl:attribute name="align">center</xsl:attribute>
					Check:
					<xsl:for-each select="text/item">
						<xsl:value-of select="something" />
						<xsl:if test="position()=last()-1">
							<xsl:text>trailing spaces    </xsl:text>
						</xsl:if>
						<xsl:if test="position()=last()-1">
							<xsl:text>    leading spaces</xsl:text>
						</xsl:if>
						<xsl:if test="position()=last()">
							<xsl:text>nospace</xsl:text>
						</xsl:if>
					</xsl:for-each>
				</p>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>