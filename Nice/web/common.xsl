<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:exsl="http://exslt.org/common"
                extension-element-prefixes="exsl"
                version="1.0">
<xsl:import 
 href="http://docbook.sourceforge.net/release/website/2.0b1/xsl/website.xsl"/>

<xsl:param name="html.stylesheet" select="'style.css'"/>
<xsl:param name="section.autolabel" select="1"/>

<!-- Usage: <javaexn name="SomeException"/> -->
<xsl:template match="javaexn">

  <xsl:variable name="link2exn">
  <xsl:element name="ulink">
    <xsl:attribute name="url">
    http://java.sun.com/j2se/1.3/docs/api/java/lang/<xsl:value-of select="@name"/>.html
    </xsl:attribute>
  <xsl:value-of select="@name"/>
  </xsl:element>
  </xsl:variable>

<!-- circumvent bug in xstlproc 
     cf http://mail.gnome.org/archives/xslt/2001-October/msg00050.html
  <xsl:apply-templates select="exsl:node-set($link2exn)"/>
-->
  <xsl:apply-templates select="exsl:node-set($link2exn)/node()"/>

</xsl:template>

</xsl:stylesheet>
