USB_AUDIO="\
	${@bb.utils.contains('DISTRO_FEATURES', 'adi_usb_gadget_audio', 'adi_usb_gadget_audio.inc', '', d)} \
"
require linux-adi.inc sharc_audio.inc ${USB_AUDIO}

LICENSE="GPL-2.0-only"
LIC_FILES_CHKSUM="file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

DEPENDS += "u-boot-mkimage-native dtc-native"

# Include kernel configuration fragments
SRC_URI:append="\
	file://feature/cfg/nfs.cfg \
	file://feature/cfg/wireless.cfg \
	file://feature/cfg/cpufreq.cfg \
	file://feature/cfg/crypto.cfg \
	file://feature/cfg/tracepoints.cfg \
"

python () {
    if ((d.getVar("ADSP_KERNEL_TYPE") == "upstream") and ("adsp-sc598" in d.getVar("MACHINE"))):
        print("Building with upstream kernel")
        d.setVar("PV","upstream")
        d.setVar("KERNEL_VERSION_SANITY_SKIP","1")
        d.setVar("KERNEL_BRANCH","adsp-main")
        d.setVar("SRCREV","${AUTOREV}")
    else:
        d.setVar("PV","5.15.148")
        d.setVar("KERNEL_BRANCH","main")
        d.setVar("SRCREV","c4403f406eff867723e10acf414afdfe8132102f")

    d.setVar("LINUX_VERSION",d.getVar("PV"))
}

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

SRC_URI:append:adsp-sc594-som-ezkit = " file://feature/cfg/snd_ezkit.scc"
SRC_URI:append:adsp-sc589-ezkit = " file://feature/cfg/snd_ezkit.scc"
SRC_URI:append:adsp-sc584-ezkit = " file://feature/cfg/snd_ezkit.scc"
SRC_URI:append:adsp-sc573-ezkit = " file://feature/cfg/snd_ezkit.scc"
SRC_URI:append:adsp-sc589-mini = " file://feature/cfg/snd_mini.scc"

# Only SC598 can trigger upstream builds
SRC_URI:append:adsp-sc598-som-ezkit = "${@' file://0001-sc598-som-enable-SDcard.patch' if (bb.utils.to_boolean(d.getVar('ADSP_SC598_SDCARD')) and (d.getVar('ADSP_KERNEL_TYPE') != 'upstream')) else ''}"

SRC_URI:append:adsp-sc598-som-ezkit = ' file://0001-SC598-fix-stmmac-dma-split-header-crash.patch'

do_install:append(){
	rm -rf ${D}/lib/modules/*-yocto-standard/modules.builtin.modinfo
}
