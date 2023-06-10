

jQuery(document).ready(function () {

	"use strict";

	// here all ready functions
	edrea_tm_my_load();
	edrea_tm_picker();
	edrea_tm_modalbox();
	edrea_tm_about_modalbox();
	edrea_tm_page_transition();
	edrea_tm_trigger_menu();
	edrea_tm_about_popup();
	edrea_tm_portfolio_popup();
	edrea_tm_news_popup();
	// edrea_tm_cursor();
	edrea_tm_imgtosvg();
	edrea_tm_popup();
	edrea_tm_data_images();
	edrea_tm_contact_form();
	hashtag();
	edrea_tm_swiper();
	efak_tm_page_play();
	edrea_tm_play_modal_popup();

	// docs
	efak_docs_function();

});

function efak_docs_function() {
	$("#efak_docs_pages").show();
	$("#efak_docs_what_is_efak").hide();
	$("#efak_docs_get_start").hide();
	$("#efak_docs_requirements").hide();
	$("#efak_docs_install_linux_macos").hide();
	$("#efak_docs_install_windows").hide();
	$("#efak_docs_config").hide();
	$("#efak_docs_security").hide();
	$("#efak_docs_dashboard").hide();
	$("#efak_docs_topics").hide();
	$("#efak_docs_consumers").hide();
	$("#efak_docs_cluster").hide();
	$("#efak_docs_alarm").hide();
	$("#efak_docs_shell").hide();
	$("#efak_docs_zkcli").hide();
	$("#efak_docs_multi").hide();
	$("#efak_docs_ksql").hide();
	$("#efak_docs_system").hide();
	$("#efak_docs_metrics").hide();
	$("#efak_docs_tv").hide();
	$("#efak_docs_arch_intro").hide();
	$("#efak_docs_arch_collect").hide();
	$("#efak_docs_arch_core").hide();
	$("#efak_docs_arch_advanced").hide();
	$("#efak_docs_changelog").hide();
}

function efak_docs_transcation(navbar_value) {
	switch (navbar_value) {
		case "what_is_efak":
			$("#efak_docs_what_is_efak").show();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;
		case "get_start":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").show();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;
		case "requirements":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").show();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;
		case "install_linux_macos":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").show();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;
		case "install_windows":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").show();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// 
		case "efak_config":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").show();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// 
		case "efak_security":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").show();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// 
		case "efak_dashboard":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").show();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// efak_docs_topics
		case "efak_topics":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").show();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// efak_docs_consumers
		case "efak_consumers":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").show();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// efak_docs_cluster
		case "efak_cluster":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").show();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// efak_docs_alarm
		case "efak_alarm":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").show();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// efak_docs_shell
		case "efak_shell":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").show();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// efak_docs_zkcli
		case "efak_zkcli":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").show();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// efak_docs_multi
		case "efak_multi":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").show();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// efak_docs_ksql
		case "efak_ksql":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").show();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// efak_docs_system
		case "efak_system":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").show();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// efak_docs_metrics
		case "efak_metrics":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").show();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// efak_docs_tv
		case "efak_tv":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").show();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// efak_docs_arch_intro
		case "efak_arch_intro":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").show();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// efak_docs_arch_collect
		case "efak_arch_collect":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").show();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// efak_docs_arch_core
		case "efak_arch_core":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").show();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").hide();
			break;// efak_docs_advanced
		case "efak_arch_advanced":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").show();
			$("#efak_docs_changelog").hide();
			break;// efak_docs_changelog
		case "efak_changelog":
			$("#efak_docs_what_is_efak").hide();
			$("#efak_docs_pages").hide();
			$("#efak_docs_get_start").hide();
			$("#efak_docs_requirements").hide();
			$("#efak_docs_install_linux_macos").hide();
			$("#efak_docs_install_windows").hide();
			$("#efak_docs_config").hide();
			$("#efak_docs_security").hide();
			$("#efak_docs_dashboard").hide();
			$("#efak_docs_topics").hide();
			$("#efak_docs_consumers").hide();
			$("#efak_docs_cluster").hide();
			$("#efak_docs_alarm").hide();
			$("#efak_docs_shell").hide();
			$("#efak_docs_zkcli").hide();
			$("#efak_docs_multi").hide();
			$("#efak_docs_ksql").hide();
			$("#efak_docs_system").hide();
			$("#efak_docs_metrics").hide();
			$("#efak_docs_tv").hide();
			$("#efak_docs_arch_intro").hide();
			$("#efak_docs_arch_collect").hide();
			$("#efak_docs_arch_core").hide();
			$("#efak_docs_arch_advanced").hide();
			$("#efak_docs_changelog").show();
			break;// efak_docs_changelog
	}
}


// click play efak
$("#efak_home_video").hide();
function click_play_efak() {
	$("#efak_home_video").show();
}

function click_play_efak_closed() {
	$("#efak_home_video").hide();
}

// -----------------------------------------------------
// ---------------   FUNCTIONS    ----------------------
// -----------------------------------------------------

// -----------------------------------------------------
// ---------------   COLOR PICKER    -------------------
// -----------------------------------------------------

function edrea_tm_picker() {

	"use strict";

	if (jQuery('.edrea_tm_settings').length) {

		// attach background for all colors
		var list = jQuery('.edrea_tm_settings .colors li a');
		list.each(function () {
			jQuery(this).css({ backgroundColor: jQuery(this).data('color') });
		});

		// change root color
		list.on('click', function () {
			var element = jQuery(this);
			var color = element.data('color');
			jQuery(':root').css('--main-color', color);
			return false;
		});
	}

}

// -------------------------------------------------
// -------------  PROGRESS BAR  --------------------
// -------------------------------------------------

function edrea_tm_my_progress() {

	"use strict";

	jQuery('.progress_inner').each(function () {
		var progress = jQuery(this);
		var pValue = parseInt(progress.data('value'), 10);
		var pColor = progress.data('color');
		var pBarWrap = progress.find('.bar');
		var pBar = progress.find('.bar_in');
		pBar.css({ width: pValue + '%', backgroundColor: pColor });
		setTimeout(function () { pBarWrap.addClass('open'); });
	});
}

// -----------------------------------------------------
// ---------------   CIRCULAR PROGRESS   ---------------
// -----------------------------------------------------

function edrea_tm_circular_progress() {

	"use strict";

	var circVal = 110;

	var colorSchemes = jQuery(':root').css('--main-color');

	jQuery('.circular_progress_bar .myCircle').each(function () {
		var element = jQuery(this);
		element.append('<span class="number"></span>');
		var value = element.data('value');
		element.circleProgress({
			size: circVal,
			value: 0,
			animation: { duration: 1400 },
			thickness: 2,
			fill: colorSchemes,
			emptyFill: 'rgba(0,0,0,0)',
			startAngle: -Math.PI / 2
		}).on('circle-animation-progress', function (event, progress, stepValue) {
			element.find('.number').text(parseInt(stepValue.toFixed(2) * 100) + '%');
		});
		element.circleProgress('value', 1.0);
		setTimeout(function () { element.circleProgress('value', value); }, 1400);
	});
}

// -----------------------------------------------------
// --------------------   MODALBOX    ------------------
// -----------------------------------------------------

function edrea_tm_modalbox() {
	"use strict";

	jQuery('.edrea_tm_all_wrap').prepend('<div class="edrea_tm_modalbox"><div class="box_inner" style=" width: 990px;height: 490px;"><div class="close"><a href="#"><i class="icon-cancel"></i></a></div><div class="description_wrap"></div></div></div>')
}

function edrea_tm_about_modalbox() {
	"use strict";

	jQuery('.edrea_tm_all_wrap').prepend('<div id="edrea_tm_modalbox_about" class="edrea_tm_modalbox"><div class="box_inner"><div class="close"><a href="#"><i class="icon-cancel"></i></a></div><div class="description_wrap"></div></div></div>')
}

// -----------------------------------------------------
// -------------   PAGE TRANSITION    ------------------
// -----------------------------------------------------

function edrea_tm_page_transition() {

	"use strict";

	var section = jQuery('.edrea_tm_section');
	var allLi = jQuery('.transition_link li');
	var button = jQuery('.transition_link a');
	var wrapper = jQuery('.edrea_tm_all_wrap');
	var enter = wrapper.data('enter');
	var exit = wrapper.data('exit');

	button.on('click', function () {
		var element = jQuery(this);
		var href = element.attr('href');
		if (element.parent().hasClass('edrea_tm_button')) {
			jQuery('.menu .transition_link a[href="' + href + '"]').trigger('click');
			hashtag();
			return false;
		}
		var sectionID = jQuery(href);
		var parent = element.closest('li');
		if (!parent.hasClass('active')) {
			allLi.removeClass('active');
			wrapper.find(section).removeClass('animated ' + enter);
			if (wrapper.hasClass('opened')) {
				wrapper.find(section).addClass('animated ' + exit);
			}
			parent.addClass('active');
			wrapper.addClass('opened');
			wrapper.find(sectionID).removeClass('animated ' + exit).addClass('animated ' + enter);
			jQuery(section).addClass('hidden');
			jQuery(sectionID).removeClass('hidden').addClass('active');
		}
		return false;
	});
}

// play 

function efak_tm_page_play() {

	"use strict";

	var section = jQuery('.edrea_tm_section');
	var allLi = jQuery('.transition_link li');
	var button = jQuery('.transition_link a');
	var wrapper = jQuery('.edrea_tm_all_wrap');
	var enter = wrapper.data('enter');
	var exit = wrapper.data('exit');

	button.on('click', function () {
		var element = jQuery(this);
		var href = element.attr('href');
		if (element.parent().hasClass('edrea_tm_button')) {
			jQuery('.menu .transition_link a[href="' + href + '"]').trigger('click');
			hashtag();
			return false;
		}
		var sectionID = jQuery(href);
		var parent = element.closest('li');
		if (!parent.hasClass('active')) {
			allLi.removeClass('active');
			wrapper.find(section).removeClass('animated ' + enter);
			if (wrapper.hasClass('opened')) {
				wrapper.find(section).addClass('animated ' + exit);
			}
			parent.addClass('active');
			wrapper.addClass('opened');
			wrapper.find(sectionID).removeClass('animated ' + exit).addClass('animated ' + enter);
			jQuery(section).addClass('hidden');
			jQuery(sectionID).removeClass('hidden').addClass('active');
		}
		return false;
	});
}

function edrea_tm_play_modal_popup() {

	"use strict";

	var button = jQuery('#efak_play_btn_app');
	var close = jQuery('.edrea_tm_modalbox .close');
	var modalBox = jQuery('.edrea_tm_modalbox');
	var hiddenContent = jQuery('#efak_tm_play_modal').html();

	button.on('click', function () {
		modalBox.addClass('opened');
		modalBox.find('.description_wrap').html(hiddenContent);
		edrea_tm_data_images();
		edrea_tm_my_progress();
		edrea_tm_circular_progress();
		edrea_tm_mycarousel();
		edrea_tm_location();
	});
	close.on('click', function () {
		modalBox.removeClass('opened');
		modalBox.find('.description_wrap').html('');
	});
}

// -----------------------------------------------------
// ---------------   TRIGGER MENU    -------------------
// -----------------------------------------------------

function edrea_tm_trigger_menu() {

	"use strict";

	var hamburger = jQuery('.edrea_tm_topbar .trigger .hamburger');
	var mobileMenu = jQuery('.edrea_tm_mobile_menu');
	var mobileMenuList = jQuery('.edrea_tm_mobile_menu ul li a');

	hamburger.on('click', function () {
		var element = jQuery(this);

		if (element.hasClass('is-active')) {
			element.removeClass('is-active');
			mobileMenu.removeClass('opened');
		} else {
			element.addClass('is-active');
			mobileMenu.addClass('opened');
		}
		return false;
	});

	mobileMenuList.on('click', function () {
		jQuery('.edrea_tm_topbar .trigger .hamburger').removeClass('is-active');
		mobileMenu.removeClass('opened');
		return false;
	});
}

// -------------------------------------------------
// ---------------  ABOUT POPUP  -------------------
// -------------------------------------------------

function edrea_tm_about_popup() {

	"use strict";

	var button = jQuery('#efak_edrea_tm_button_about');
	var close = jQuery('.edrea_tm_about_modalbox .close');
	var modalBox = jQuery('#edrea_tm_modalbox_about');
	var hiddenContent = jQuery('#efak_hidden_content_about').html();

	button.on('click', function () {
		modalBox.addClass('opened');
		modalBox.find('.description_wrap').html(hiddenContent);
		edrea_tm_data_images();
		edrea_tm_my_progress();
		edrea_tm_circular_progress();
		edrea_tm_mycarousel();
		edrea_tm_location();
	});
	close.on('click', function () {
		modalBox.removeClass('opened');
		modalBox.find('.description_wrap').html('');
	});
}

// -------------------------------------------------
// -----------  PORTFOLIO POPUP  -------------------
// -------------------------------------------------

function edrea_tm_portfolio_popup() {

	"use strict";

	var modalBox = jQuery('.edrea_tm_modalbox');
	var button = jQuery('.edrea_tm_portfolio .portfolio_popup');
	var closePopup = modalBox.find('.close');

	button.off().on('click', function () {
		var element = jQuery(this);
		var parent = element.closest('.list_inner');
		var content = parent.find('.edrea_tm_hidden_content').html();
		var image = parent.find('.image .main').data('img-url');
		var title = parent.find('.details h3').text();
		var category = parent.find('.details span').text();
		modalBox.addClass('opened');
		modalBox.find('.description_wrap').html(content);
		modalBox.find('.portfolio_popup_details').prepend('<div class="top_image"><img src="img/thumbs/4-2.jpg" alt="" /><div class="main" data-img-url="' + image + '"></div></div>');
		modalBox.find('.portfolio_popup_details .top_image').after('<div class="portfolio_main_title"><h3>' + title + '</h3><span><a href="#">' + category + '</a></span><div>');
		edrea_tm_data_images();
		edrea_tm_popup();
		return false;
	});
	closePopup.on('click', function () {
		modalBox.removeClass('opened');
		modalBox.find('.description_wrap').html('');
		return false;
	});
}

// -------------------------------------------------
// ----------------  NEWS POPUP  -------------------
// -------------------------------------------------

function edrea_tm_news_popup() {

	"use strict";

	var modalBox = jQuery('.edrea_tm_modalbox');
	var button = jQuery('.edrea_tm_news .news_popup,.edrea_tm_news .news_list h3 a');
	var closePopup = modalBox.find('.close');

	button.off().on('click', function () {
		var element = jQuery(this);
		var parent = element.closest('.list_inner');
		var content = parent.find('.edrea_tm_hidden_content').html();
		var image = parent.find('.image .main').data('img-url');
		var title = parent.find('.details h3 a').text();
		var category = parent.find('.details span').html();
		modalBox.addClass('opened');
		modalBox.find('.description_wrap').html(content);
		modalBox.find('.news_popup_details').prepend('<div class="top_image"><img src="img/thumbs/4-2.jpg" alt="" /><div class="main" data-img-url="' + image + '"></div></div>');
		modalBox.find('.news_popup_details .top_image').after('<div class="news_main_title"><h3>' + title + '</h3><span>' + category + '</span><div>');
		edrea_tm_data_images();
		return false;
	});
	closePopup.on('click', function () {
		modalBox.removeClass('opened');
		modalBox.find('.description_wrap').html('');
		return false;
	});
}

// -----------------------------------------------------
// ---------------   PRELOADER   -----------------------
// -----------------------------------------------------

function edrea_tm_preloader() {

	"use strict";

	var isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent) ? true : false;
	var preloader = $('#preloader');

	if (!isMobile) {
		setTimeout(function () {
			preloader.addClass('preloaded');
		}, 100);
		setTimeout(function () {
			preloader.remove();
		}, 200);

	} else {
		preloader.remove();
	}
}

// -----------------------------------------------------
// -----------------   MY LOAD    ----------------------
// -----------------------------------------------------

function edrea_tm_my_load() {

	"use strict";

	var speed = 100;
	setTimeout(function () { edrea_tm_preloader(); }, speed);
}

// -----------------------------------------------------
// ------------------   CURSOR    ----------------------
// -----------------------------------------------------

// -----------------------------------------------------
// ---------------    IMAGE TO SVG    ------------------
// -----------------------------------------------------

function edrea_tm_imgtosvg() {

	"use strict";

	jQuery('img.svg').each(function () {

		var jQueryimg = jQuery(this);
		var imgClass = jQueryimg.attr('class');
		var imgURL = jQueryimg.attr('src');

		jQuery.get(imgURL, function (data) {
			// Get the SVG tag, ignore the rest
			var jQuerysvg = jQuery(data).find('svg');

			// Add replaced image's classes to the new SVG
			if (typeof imgClass !== 'undefined') {
				jQuerysvg = jQuerysvg.attr('class', imgClass + ' replaced-svg');
			}

			// Remove any invalid XML tags as per http://validator.w3.org
			jQuerysvg = jQuerysvg.removeAttr('xmlns:a');

			// Replace image with new SVG
			jQueryimg.replaceWith(jQuerysvg);

		}, 'xml');

	});
}

// -----------------------------------------------------
// --------------------   POPUP    ---------------------
// -----------------------------------------------------

function edrea_tm_popup() {

	"use strict";

	jQuery('.gallery_zoom').each(function () { // the containers for all your galleries
		jQuery(this).magnificPopup({
			delegate: 'a.zoom', // the selector for gallery item
			type: 'image',
			gallery: {
				enabled: true
			},
			removalDelay: 300,
			mainClass: 'mfp-fade'
		});

	});
	jQuery('.popup-youtube, .popup-vimeo').each(function () { // the containers for all your galleries
		jQuery(this).magnificPopup({
			disableOn: 700,
			type: 'iframe',
			mainClass: 'mfp-fade',
			removalDelay: 160,
			preloader: false,
			fixedContentPos: false
		});
	});

	jQuery('.soundcloude_link').magnificPopup({
		type: 'image',
		gallery: {
			enabled: true,
		},
	});
}

// -----------------------------------------------------
// ---------------   DATA IMAGES    --------------------
// -----------------------------------------------------

function edrea_tm_data_images() {

	"use strict";

	var data = jQuery('*[data-img-url]');

	data.each(function () {
		var element = jQuery(this);
		var url = element.data('img-url');
		element.css({ backgroundImage: 'url(' + url + ')' });
	});
}

// -----------------------------------------------------
// ----------------    CONTACT FORM    -----------------
// -----------------------------------------------------

function edrea_tm_contact_form() {

	"use strict";

	jQuery(".contact_form #send_message").on('click', function () {

		var name = jQuery(".contact_form #name").val();
		var email = jQuery(".contact_form #email").val();
		var message = jQuery(".contact_form #message").val();
		var subject = jQuery(".contact_form #subject").val();
		var success = jQuery(".contact_form .returnmessage").data('success');

		jQuery(".contact_form .returnmessage").empty(); //To empty previous error/success message.
		//checking for blank fields	
		if (name === '' || email === '' || message === '') {

			jQuery('div.empty_notice').slideDown(500).delay(2000).slideUp(500);
		}
		else {
			// Returns successful data submission message when the entered information is stored in database.
			jQuery.post("modal/contact.php", { ajax_name: name, ajax_email: email, ajax_message: message, ajax_subject: subject }, function (data) {

				jQuery(".contact_form .returnmessage").append(data);//Append returned message to message paragraph


				if (jQuery(".contact_form .returnmessage span.contact_error").length) {
					jQuery(".contact_form .returnmessage").slideDown(500).delay(2000).slideUp(500);
				} else {
					jQuery(".contact_form .returnmessage").append("<span class='contact_success'>" + success + "</span>");
					jQuery(".contact_form .returnmessage").slideDown(500).delay(4000).slideUp(500);
				}

				if (data === "") {
					jQuery("#contact_form")[0].reset();//To reset form fields on success
				}

			});
		}
		return false;
	});
}

// -----------------------------------------------------
// --------------    OWL CAROUSEL    -------------------
// -----------------------------------------------------

function edrea_tm_mycarousel() {

	"use strict";

	var carousel = jQuery('.edrea_tm_modalbox .owl-carousel');

	carousel.owlCarousel({
		loop: true,
		items: 1,
		lazyLoad: false,
		margin: 0,
		autoplay: true,
		autoplayTimeout: 7000,
		dots: false,
		nav: false,
		navSpeed: false,
		responsive: {
			0: {
				items: 1
			},
			768: {
				items: 1
			}
		}
	});

}

// -----------------------------------------------------
// -------------------    HASHTAG    -------------------
// -----------------------------------------------------

function hashtag() {
	"use strict";
	var ccc = $('.edrea_tm_header .menu .ccc');
	var element = $('.edrea_tm_header .menu .active a');
	$('.edrea_tm_header .menu a').on('mouseenter', function () {
		var e = $(this);
		currentLink(ccc, e);
	});
	$('.edrea_tm_header .menu').on('mouseleave', function () {
		element = $('.edrea_tm_header .menu .active a');
		currentLink(ccc, element);
		element.parent().siblings().removeClass('mleave');
	});
	currentLink(ccc, element);

}

function currentLink(ccc, e) {
	"use strict";
	if (!e.length) { return false; }
	var left = e.offset().left;
	var width = e.outerWidth();
	var menuleft = $('.edrea_tm_header .menu').offset().left;
	e.parent().removeClass('mleave');
	e.parent().siblings().addClass('mleave');
	ccc.css({ left: (left - menuleft) + 'px', width: width + 'px' });

}

// -----------------------------------------------------
// ---------------   SWIPER SLIDER    ------------------
// -----------------------------------------------------

function edrea_tm_swiper() {
	"use strict";

	$('.swiper-section').each(function () {
		var element = $(this);
		var container = element.find('.swiper-container');
		var mySwiper = new Swiper(container, {
			loop: false,
			slidesPerView: 1,
			spaceBetween: 0,
			loopAdditionalSlides: 1,
			autoplay: {
				delay: 6000,
			},

			navigation: {
				nextEl: '.my_next',
				prevEl: '.my_prev',
			},

			pagination: {
				el: '.edrea_tm_swiper_progress',
				type: 'custom', // progressbar
				renderCustom: function (swiper, current, total) {


					// progress animation
					var scale, translateX;
					var progressDOM = container.find('.edrea_tm_swiper_progress');
					if (progressDOM.hasClass('fill')) {
						translateX = '0px';
						scale = parseInt((current / total) * 100) / 100;
					} else {
						scale = parseInt((1 / total) * 100) / 100;
						translateX = (current - 1) * parseInt((100 / total) * 100) / 100 + 'px';
					}


					progressDOM.find('.all span').css({ transform: 'translate3d(' + translateX + ',0px,0px) scaleX(' + scale + ') scaleY(1)' });
					if (current < 10) { current = '0' + current; }
					if (total < 10) { total = '0' + total; }
					progressDOM.find('.current').html(current);
					progressDOM.find('.total').html(total);
				}
			},
			breakpoints: {
				700: {
					slidesPerView: 2,
					spaceBetween: 20,
				},
				1200: {
					slidesPerView: 3,
					spaceBetween: 30,
				}
			}
		});
	});
	edrea_tm_imgtosvg();
}

// -------------------------------------------------
// -----------------  LOCATION  --------------------
// -------------------------------------------------

function edrea_tm_location() {

	"use strict";

	var button = jQuery('.href_location');
	button.on('click', function () {
		var element = jQuery(this);
		var address = element.text();
		address = address.replace(/\ /g, '+');
		var text = 'https://maps.google.com?q=';
		window.open(text + address);
		return false;
	});
}

// -----------------------------------------------------
// ---------------------   SWITCHERS    ----------------
// -----------------------------------------------------

function edrea_tm_color_switcher() {

	"use strict";

	var list = jQuery('.edrea_tm_settings .colors li a');

	list.on('click', function () {
		var element = jQuery(this);
		var elval = element.attr('class');
		element.closest('.edrea_tm_all_wrap').attr('data-color', '#4169e1');
		//		edrea_tm_circular_progress();
		return false;
	});
}

