PROC=xsltproc
STYLEDIR=/usr/share/sgml/docbook/custom/website/2.0b1/xsl
STYLESHEET=common.xsl
STYLEOPT=
DOCBK=/usr/share/sgml/docbook/stylesheet/xsl/nwalsh/html/docbook.xsl

.PHONY : clean

all: index.html research.html compilation.html testimonials.html install.html \
     roadmap.html

old: safety.html

manual.html: manual.xml $(DOCBK)
	$(PROC) $(DOCBK) manual.xml > $@

%.html: %.xml new.xsl
	$(PROC) new.xsl $*.xml $(STYLEOPT) > $@

# Old style
safety.html: %.xml $(STYLESHEET)
	$(PROC) $(STYLESHEET) $*.xml $(STYLEOPT) > $@
