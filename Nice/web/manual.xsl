<?xml version='1.0'?>

<!--

	Stylesheet for the Nice User's Manual.

  This is based on the dookbook XSL stylesheets.
  Currently, it just overrides the translation of <type> elements, 
  to make them rendered in a monospace font.
  Other customisations can be done in this file if necessary.

-->

<!DOCTYPE xsl:stylesheet>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'
                xmlns="http://www.w3.org/TR/xhtml1/transitional"
                xmlns:exsl="http://exslt.org/common"
                extension-element-prefixes="exsl"
                exclude-result-prefixes="#default">

<xsl:import href="http://docbook.sourceforge.net/release/xsl/current/html/docbook.xsl"/>

<xsl:template match="type">
  <xsl:call-template name="inline.monoseq"/>
</xsl:template>

<!-- Usage: <javaexn name="SomeException"/> -->

<xsl:template match="javaexn">

  <xsl:element name="a">
    <xsl:attribute name="href">
    http://java.sun.com/j2se/1.3/docs/api/java/lang/<xsl:value-of select="@name"/>.html
    </xsl:attribute>
  <xsl:value-of select="@name"/>
  </xsl:element>

</xsl:template>

<!-- In no.anchor.mode, just copy the name of the exception. -->
<xsl:template match="title//javaexn" mode="no.anchor.mode">
  <xsl:value-of select="@name"/>
</xsl:template>

</xsl:stylesheet>
