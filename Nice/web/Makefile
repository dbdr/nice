# Commands to build the website and the documentation.
#
# Requires the following external programs and packages:
#   xmlto, xsltproc, docbook stylesheets (http://docbook.sourceforge.net/),
#   xmltex, passivetex.
#
# On a Debian system, you can get all of these by running:
#   apt-get install xmlto docbook-xsl passivetex


PROC=xsltproc --nonet
STYLESHEET=common.xsl
STYLEOPT=

.PHONY : clean send

all: index.html research.html compilation.html testimonials.html install.html \
     roadmap.html manual.html manual.pdf manual.ps

send: all
	scp -r *.html *.pdf *.ps *.css .htaccess images bonniot@shell.sourceforge.net:/home/groups/n/ni/nice/htdocs

old: safety.html

manual.html: manual.xsl manual.xml
	$(PROC) manual.xsl manual.xml > $@

manual.pdf manual.ps: manual.xml
	xmlto $(shell expr match $@ 'manual\.\(.*\)') manual.xml

%.html: %.xml new.xsl
	$(PROC) new.xsl $*.xml $(STYLEOPT) > $@

# Old style
safety.html: %.xml $(STYLESHEET)
	$(PROC) $(STYLESHEET) $*.xml $(STYLEOPT) > $@
