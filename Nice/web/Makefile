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
     irc.html roadmap.html safety.html manual.html manual.pdf manual.ps

send: all send_manual_pdf send_manual_ps
	scp -r *.css *.html .htaccess images bonniot@shell.sourceforge.net:/home/groups/n/ni/nice/htdocs

send_manual_pdf: manual.pdf
	scp manual.pdf bonniot@shell.sourceforge.net:/home/groups/n/ni/nice/htdocs/NiceManual.pdf
	touch send_manual_pdf

send_manual_ps: manual.ps
	scp manual.ps bonniot@shell.sourceforge.net:/home/groups/n/ni/nice/htdocs/NiceManual.ps
	touch send_manual_ps

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
