PROC=xsltproc
STYLEDIR=/usr/share/sgml/docbook/custom/website/2.0b1/xsl
STYLESHEET=common.xsl
STYLEOPT=
DOCBK=/usr/share/sgml/docbook/stylesheet/xsl/nwalsh/html/docbook.xsl

.PHONY : clean send

all: index.html research.html compilation.html testimonials.html install.html \
     roadmap.html manual.html

send: all
	scp -r *.html *.css .htaccess images bonniot@shell.sourceforge.net:/home/groups/n/ni/nice/htdocs

old: safety.html

manual.html: manual.xml $(DOCBK)
	$(PROC) $(DOCBK) manual.xml > $@

%.html: %.xml new.xsl
	$(PROC) new.xsl $*.xml $(STYLEOPT) > $@

# Old style
safety.html: %.xml $(STYLESHEET)
	$(PROC) $(STYLESHEET) $*.xml $(STYLEOPT) > $@
