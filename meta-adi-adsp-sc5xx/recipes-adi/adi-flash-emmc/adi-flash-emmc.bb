
LICENSE = "CLOSED"

SRC_URI += " \
	file://adi-flash-emmc.sh \
"

do_install(){
	install -d ${D}/usr/bin
	install -m 0777 ${WORKDIR}/adi-flash-emmc.sh ${D}/usr/bin
}

FILES:${PN} += " \
	/usr/bin/adi-flash-emmc.sh \
"

