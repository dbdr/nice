# Commands to build the website and the documentation.
#
# Requires the following external programs and packages:
#   xmlto, xsltproc, docbook stylesheets (http://docbook.sourceforge.net/),
#   xmltex, passivetex.
#
# On a Debian system, you can get all of these by running:
#   apt-get install xmlto docbook-xsl passivetex


PROC=xsltproc --nonet

.PHONY : clean send

all: index.html research.html compilation.html testimonials.html install.html \
     roadmap.html safety.html manual.html manual.pdf manual.ps

send: all
	scp -r *.css *.html .htaccess images *.pdf *.ps bonniot@shell.sourceforge.net:/home/groups/n/ni/nice/htdocs

old: safety.html

manual.html: manual.xsl manual.xml
	if ! $(PROC) manual.xsl manual.xml > $@; then rm manual.html; fi

manual.pdf manual.ps: manual.xml
	xmlto $(shell expr match $@ 'manual\.\(.*\)') manual.xml

%.html: %.xml new.xsl
	$(PROC) new.xsl $*.xml > $@

# Old style
safety.html: manual.xsl safety.xml
	if ! $(PROC) manual.xsl safety.xml > $@; then rm safety.html; fi
