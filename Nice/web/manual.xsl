<?xml version='1.0'?>

<!--

	Stylesheet for the Nice User's Manual.

  This is based on the dookbook XSL stylesheets.
  Currently, it just overrides the translation of <type> elements, 
  to make them rendered in a monospace font.
  Other customisations can be done in this file if necessary.

-->

<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#xA0;"> <!ENTITY quest "&#x3F;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'
                xmlns="http://www.w3.org/TR/xhtml1/transitional"
                exclude-result-prefixes="#default">

<xsl:import href="http://docbook.sourceforge.net/release/xsl/current/html/docbook.xsl"/>

<xsl:template match="type">
  <xsl:call-template name="inline.monoseq"/>
</xsl:template>

</xsl:stylesheet>
