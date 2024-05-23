inherit adsp-sc5xx-compatible

require u-boot-adi.inc

SRCREV = "faba0cf1f35f65ff488c0a44258a9db41a56bef9"

UBOOT_INITIAL_ENV = ""

STAGE_1_TARGET_NAME = "stage1-boot.ldr"

FILES:${PN} = " \
    u-boot-proper-${BOARD}.elf \
    u-boot-spl-${BOARD}.elf \
    ${STAGE_1_TARGET_NAME} \
"

EXTRA_OEMAKE += "LDR=ldr"

python () {
    # machine must have a som and ezkit
    if (not (("som" in d.getVar("MACHINE")) and ("ezkit" in d.getVar("MACHINE")))):
        d.setVar("CUSTOM_REV", 0)
        return

    # get config name
    d.setVar("REV_CFG", d.getVar("MACHINE") + d.getVar("SOM_REV") + d.getVar("CRR_REV") + ".cfg")
    d.setVar("CUSTOM_REV", 1)
    #get which som and carrier are being used
    som = (d.getVar("MACHINE").split("som")[-2].split("-")[-2]).upper()
    crr = (d.getVar("MACHINE").split("som")[-1].split("-")[-1]).upper()
    # generate temporary cfg
    with open(d.expand("${THISDIR}") + "/" + d.getVar("REV_CFG"), "w") as rev_patch:
        som_rev_config = "CONFIG_TARGET_" + som + "_SOM_REV_" + d.getVar("SOM_REV") + "=y\n"
        crr_rev_config = "CONFIG_TARGET_" + crr + "_CRR_REV_" + d.getVar("CRR_REV") + "=y"
        final_config = som_rev_config + crr_rev_config
        rev_patch.write(final_config)
}

FILESEXTRAPATHS:prepend := "${THISDIR}/:"
SRC_URI:append = "${@'file://${REV_CFG}' if (bb.utils.to_boolean(d.getVar('CUSTOM_REV'))) else ''}"


do_install () {
	install ${B}/u-boot-proper-${BOARD}.elf ${D}/
	install ${B}/u-boot-spl-${BOARD}.elf ${D}/
	install ${B}/u-boot-spl-${BOARD}.ldr ${D}/${STAGE_1_TARGET_NAME}
}

do_deploy() {
	install ${B}/u-boot-proper-${BOARD}.elf ${DEPLOYDIR}/
	install ${B}/u-boot-spl-${BOARD}.elf ${DEPLOYDIR}/
	install ${B}/u-boot-spl-${BOARD}.ldr ${DEPLOYDIR}/${STAGE_1_TARGET_NAME}
}

INSANE_SKIP:${PN} += "textrel"
