PROC=xsltproc
STYLEDIR=/usr/share/sgml/docbook/custom/website/2.0b1/xsl
STYLESHEET=common.xsl
STYLEOPT=

.PHONY : clean

all: index.html research.html compilation.html testimonials.html

old: safety.html install.html

%.html: %.xml new.xsl
	$(PROC) new.xsl $*.xml $(STYLEOPT) > $@

include depends.tabular

autolayout.xml: layout.xml
	$(PROC) $(STYLEDIR)/autolayout.xsl $< > $@
	make depends

# for the moment don't use layout, as I'm not sure what it should look like
%.html: %.xml $(STYLESHEET) #autolayout.xml 
	$(PROC) $(STYLESHEET) $*.xml $(STYLEOPT) > $@

depends: autolayout.xml
	$(PROC) $(STYLEDIR)/makefile-dep.xsl $< > depends.tabular
