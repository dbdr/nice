PROC=xsltproc
STYLEDIR=/usr/share/sgml/docbook/custom/website/2.0b1/xsl
STYLESHEET=common.xsl
STYLEOPT=

.PHONY : clean

all: index.html research.html compilation.html testimonials.html install.html

old: safety.html

%.html: %.xml new.xsl
	$(PROC) new.xsl $*.xml $(STYLEOPT) > $@

# Old style
safety.html: %.xml $(STYLESHEET)
	$(PROC) $(STYLESHEET) $*.xml $(STYLEOPT) > $@
