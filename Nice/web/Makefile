PROC=xsltproc --nonet
STYLESHEET=common.xsl
STYLEOPT=

.PHONY : clean send

all: index.html research.html compilation.html testimonials.html install.html \
     roadmap.html manual.html

send: all
	scp -r *.html *.css .htaccess images bonniot@shell.sourceforge.net:/home/groups/n/ni/nice/htdocs

old: safety.html

manual.html: manual.xsl manual.xml
	$(PROC) manual.xsl manual.xml > $@

%.html: %.xml new.xsl
	$(PROC) new.xsl $*.xml $(STYLEOPT) > $@

# Old style
safety.html: %.xml $(STYLESHEET)
	$(PROC) $(STYLESHEET) $*.xml $(STYLEOPT) > $@
