<?xml version='1.0'?>
<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#xA0;"> <!ENTITY quest "&#x3F;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'
                xmlns="http://www.w3.org/TR/xhtml1/transitional"
                exclude-result-prefixes="#default">

<xsl:import href="http://docbook.sourceforge.net/release/xsl/current/html/docbook.xsl"/>

<xsl:param name="css.decoration">0</xsl:param>


<xsl:output method="html"
            indent="no"
            doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
            doctype-system="http://www.w3.org/TR/html4/loose.dtd"
/>

<xsl:template name="head.content">
  <title>
  The Nice programming language
  </title>
  <xsl:element name="meta">
    <xsl:attribute name="name">keywords</xsl:attribute>
    <xsl:attribute name="content"><xsl:value-of select="./keywords"/>
    </xsl:attribute>
  </xsl:element>

  <xsl:element name="meta">
    <xsl:attribute name="name">description</xsl:attribute>
    <xsl:attribute name="content"><xsl:value-of select="./description"/>
    </xsl:attribute>
  </xsl:element>

  <link href="gray.css" rel="stylesheet" type="text/css" />
</xsl:template>

<xsl:template match="page/keywords" />
<xsl:template match="page/description" />

<xsl:template match="page">
  <!-- body table -->
  <table align="center" border="0" cellpadding="8" cellspacing="0" width="90%">
   <!-- top header -->
   <tr>
    <td valign="middle" width="25%" colspan="2">
     <!-- title table -->
     <table align="center" bgcolor="#000000" border="0" cellpadding="1" cellspacing="0" width="100%">
      <tr>
       <td valign="middle">
        <table bgcolor="#ffffff" border="0" cellpadding="8" cellspacing="0" width="100%">
         <tr>
          <td valign="middle">
           <h1>&nbsp;The Nice programming language</h1>
          </td>
         </tr>
        </table>
       </td>
      </tr>
     </table>
     <!-- end title table -->
    </td>
   </tr>
   <!-- end top header -->
 
   <tr>
    <!-- left column -->
    <td valign="top" width="23%">
     <!-- sidebox -->
     <table bgcolor="#000000" border="0" cellpadding="0" cellspacing="0" width="100%">
      <tr>
       <td>
        <table border="0" cellpadding="3" cellspacing="1" width="100%">
         <tr>
          <td bgcolor="#cccccc"><img width="48" height="48" align="middle" src="images/doc-48.png" alt="."/>
           &nbsp;<span class="title">Documentation</span>
          </td>
         </tr>
         <tr>
          <td bgcolor="#ffffff">
           &nbsp;<span class="small">o</span><xsl:text> </xsl:text>
           <a class="nav" href="index.html">Presentation</a>
           <br />
           &nbsp;<span class="small">o</span><xsl:text> </xsl:text>
           <a class="nav" href="language.html">Tutorial</a>
           <br />
           &nbsp;<span class="small">o</span><xsl:text> </xsl:text>
           <a class="nav" href="manual.html">Manual</a>
           (<a class="nav" href="manual.html">online</a>,
            <a class="nav" href="NiceManual.pdf">PDF</a>,
            <a class="nav" href="NiceManual.ps">PS</a>)
           <br />
           &nbsp;<span class="small">o</span><xsl:text> </xsl:text>
           <a class="nav" href="/cgi-bin/twiki/view/Doc/QuickIntroduction">Getting started</a>
           <br />
           &nbsp;<span class="small">o</span><xsl:text> </xsl:text>
           <a class="nav" href="compilation.html">Using the compiler</a>
           <br />
          </td>
         </tr>
        </table>
       </td>
      </tr>
     </table>
     <!-- end sidebox -->
     <br />

     <!-- sidebox -->
     <table bgcolor="#000000" border="0" cellpadding="0" cellspacing="0" width="100%">
      <tr>
       <td>
        <table border="0" cellpadding="3" cellspacing="1" width="100%">
         <tr>
          <td bgcolor="#cccccc"><img width="48" height="48" align="middle" src="images/floppy-48.png" alt="."/>
           &nbsp;<span class="title"><a class="nav" href="install.html">Download</a></span>
          </td>
         </tr>
        </table>
       </td>
      </tr>
     </table>
     <!-- end sidebox -->
     <br />

     <!-- sidebox -->
     <table bgcolor="#000000" border="0" cellpadding="0" cellspacing="0" width="100%">
      <tr>
       <td>
        <table border="0" cellpadding="3" cellspacing="1" width="100%">
         <tr>
          <td bgcolor="#cccccc"><img width="48" height="48" align="middle" src="images/envelope-48.png" alt="."/>
           &nbsp;<span class="title">User's corner</span>
          </td>
         </tr>
         <tr>
          <td bgcolor="#ffffff">
           &nbsp;<span class="small">o</span><xsl:text> </xsl:text>
           <a class="nav" href="/cgi-bin/twiki/view/Doc/WebHome">
           Collaborative site (Wiki)</a>

           <br />
           &nbsp;<span class="small">o</span><xsl:text> </xsl:text>
           <a class="nav" href="http://sourceforge.net/forum/forum.php?forum_id=40268">Forums</a>

           <br />
           &nbsp;<span class="small">o</span><xsl:text> </xsl:text>
           <a class="nav" href="http://lists.sourceforge.net/lists/listinfo/nice-info">Mailing List</a>

           <br />
           &nbsp;<span class="small">o</span><xsl:text> </xsl:text>
           <a class="nav" href="irc.html">
           Online discussion</a>
<!--
           <br />
           &nbsp;<span class="small">o</span><xsl:text> </xsl:text>
           <a class="nav" href="http://sourceforge.net/news/&quest;group_id=12788">News</a>
-->
           <br />
           &nbsp;<span class="small">o</span><xsl:text>&nbsp;</xsl:text>
           <a class="nav" href="http://sourceforge.net/project/filemodule_monitor.php&quest;filemodule_id=11365">Notify&nbsp;me&nbsp;of&nbsp;new&nbsp;versions</a>

           <br />
           &nbsp;<span class="small">o</span><xsl:text> </xsl:text>
           <a class="nav" href="http://sourceforge.net/tracker/&quest;func=add&amp;group_id=12788&amp;atid=112788">Report a bug</a>
           <br />
          </td>
         </tr>
        </table>
       </td>
      </tr>
     </table>
     <!-- end sidebox -->
     <br />

     <!-- sidebox -->
     <table bgcolor="#000000" border="0" cellpadding="0" cellspacing="0" width="100%">
      <tr>
       <td>
        <table border="0" cellpadding="3" cellspacing="1" width="100%">
         <tr>
          <td bgcolor="#cccccc"><img width="48" height="48" align="middle" src="images/dev-48.png" alt="."/>
           &nbsp;<span class="title">Development</span>
          </td>
         </tr>
         <tr>
          <td bgcolor="#ffffff">
           &nbsp;<span class="small">o</span><xsl:text> </xsl:text>
           <a class="nav" href="roadmap.html">Roadmap</a>
           <br />
           &nbsp;<span class="small">o</span><xsl:text> </xsl:text>
           <a class="nav" href="tests.html">Tests</a>
           <br />
           &nbsp;<span class="small">o</span><xsl:text> </xsl:text>
           <a class="nav" href="coverage/">Coverage</a>
           <br />
           &nbsp;<span class="small">o</span><xsl:text>&nbsp;</xsl:text>
           <a class="nav" href="/cgi-bin/twiki/view/Dev/WebHome">Wiki</a>
           <br />
           &nbsp;<span class="small">o</span><xsl:text> </xsl:text>
           <a class="nav" href="http://sourceforge.net/cvs/?group_id=12788">CVS</a>
           <br />
           &nbsp;<span class="small">o</span><xsl:text> </xsl:text>
           <a class="nav" href="http://nice.sourceforge.net/cgi-bin/twiki/view/Doc/GetInvolved">Contribute!</a>
           <br />
          </td>
         </tr>
        </table>
       </td>
      </tr>
     </table>
     <!-- end sidebox -->
     <br />

     <!-- sidebox -->
     <table bgcolor="#000000" border="0" cellpadding="0" cellspacing="0" width="100%">
      <tr>
       <td>
        <table border="0" cellpadding="3" cellspacing="1" width="100%">
         <tr>
          <td bgcolor="#cccccc"><img width="48" height="48" align="middle" src="images/academic-48.png" alt="."/>
           &nbsp;<span class="title"><a class="nav" href="research.html">Academic Research</a></span>
          </td>
         </tr>
        </table>
       </td>
      </tr>
     </table>
     <!-- end sidebox -->
     <br />

     <!-- SEARCH sidebox -->
     <table bgcolor="#000000" border="0" cellpadding="0" cellspacing="0" width="100%">
      <tr>
       <td>
        <table border="0" cellpadding="3" cellspacing="1" width="100%">
         <tr>
          <td bgcolor="#cccccc">&nbsp;

<div align="center">
<form method="get" action="http://www.google.com/custom">
  <input type="text" name="q" class="find-text" maxlength="255" value="" />
  <input type="hidden" name="domains" value="nice.sourceforge.net" />
  <input type="hidden" name="sitesearch" value="nice.sourceforge.net" />
  <input type="submit" name="sa" class="find-submit" value="Search" />
</form>
</div>

          </td>
         </tr>
        </table>
       </td>
      </tr>
     </table>
     <!-- end SEARCH sidebox -->
     <br />

     <div align="center">
     <a href="http://sourceforge.net/projects/nice">
     <img 
      src="http://sourceforge.net/sflogo.php&quest;group_id=12788&amp;type=1" 
      width="88" height="31" border="0" alt="SourceForge"/>
     </a>
     </div>


    </td>
    <!-- end left column -->

    <!-- right column -->
    <td valign="top" width="77%">
     <table border="0" cellpadding="0" cellspacing="0" width="100%">
      <tr>
       <td valign="top" width="100%">

        <!-- story -->
        <table align="center" bgcolor="#000000" border="0" cellPadding="0" cellSpacing="0" width="100%">
         <tr>
          <td>
           <table border="0" cellpadding="8" cellspacing="1" width="100%">
            <tr>
             <td bgcolor="#ffffff">

<xsl:apply-templates/>

             </td>
            </tr>
           </table>
          </td>
         </tr>
        </table>
        <!-- end story -->

       </td>
      </tr>
     </table>
    </td>

    <!-- end right column -->
   </tr>

   <tr>
    <td align="center" colspan="2">
     <!-- footer -->
       <span class="footer">
        <a class="nav" href="mailto:Daniel.Bonniot@inria.fr">Daniel Bonniot</a>

       </span>
     <!-- end footer -->
    </td>
   </tr>
  </table>
  <!-- end body table -->

</xsl:template>

<xsl:template match="page/title">
<h2><xsl:apply-templates/></h2>
</xsl:template>

<xsl:template match="blockquote/title">
<h3><xsl:apply-templates/></h3>
</xsl:template>

<xsl:template match="irc-users">
  <script language="JavaScript" src="http://searchirc.com/searchirc_chan_stats.php?n=59&amp;c=I25pY2U=&amp;o=1"></script>
</xsl:template>


</xsl:stylesheet>
