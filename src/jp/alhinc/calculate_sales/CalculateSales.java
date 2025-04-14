package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
	private static final String FILE_NOT_LINE = "売上ファイル名が連番になっていません";
	private static final String FILE_OVER = "合計金額が10桁を超えました";
	private static final String FILE_NOT_CODE = "<該当ファイル名>の支店コードが不正です";
	private static final String FILE_NAME_OVER = "<該当ファイル名>のフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
		    //コマンドライン引数が1つ設定されていなかった場合は、
		    //エラーメッセージをコンソールに表⽰します。
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();



		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}
		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		// 指定した「売上集計課題」ファイルに含まれるすべてのファイルを参照している
		File[] files = new File(args[0]).listFiles();

		// 条件に当てはまったものをListに追加するためのリストを作る
		List<File> rcdFiles = new ArrayList<>();

		//指定した「売上集計課題」にあるファイルの数だけ繰り返される。ファイル名が一致するものがあればListに追加
		for(int i = 0; i < files.length ; i++) {
				//エラー処理3-2
				if(files[i].isFile() && files[i].getName().matches("^[0-9]{8}.rcd$")) {
					rcdFiles.add(files[i]);
			}
		}
			//エラー処理2-1
			//⽐較回数は売上ファイルの数よりも1回少ないため、
			//繰り返し回数は売上ファイルのリストの数よりも1つ⼩さい数です。
		for(int i = 0; i < rcdFiles.size() - 1; i++) {
			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));
			//⽐較する2つのファイル名の先頭から数字の8⽂字を切り出し、int型に変換します。
			if((latter - former) != 1) {
				//2つのファイル名の数字を⽐較して、差が1ではなかったら、
				//エラーメッセージをコンソールに表⽰します。
				System.out.println(FILE_NOT_LINE);
				return;
			}
		}

		BufferedReader br = null;
		for(int i = 0; i < rcdFiles.size(); i++) {
			try {
				File file = rcdFiles.get(i);
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				String line;

				List<String> salesFile = new ArrayList<>();
				while((line = br.readLine()) != null) {
					salesFile.add(line);
				}

				//エラー処理2-3
				if (!branchNames.containsKey(salesFile.get(0))) {
				    //⽀店情報を保持しているMapに売上ファイルの⽀店コードが存在しなかった場合は、
				    //エラーメッセージをコンソールに表⽰します。
					System.out.println(FILE_NOT_CODE);
					return;

				}
				//エラー処理2-4
				if(salesFile.size() != 2){
				    //売上ファイルの⾏数が2⾏ではなかった場合は、
				    //エラーメッセージをコンソールに表⽰します。
					System.out.println(FILE_NAME_OVER);
					return;
				}
				//エラー処理3-3
				if(!salesFile.get(1).matches(("^[0-9]*$"))) {
				    //売上⾦額が数字ではなかった場合は、
				    //エラーメッセージをコンソールに表⽰します。
					System.out.println(UNKNOWN_ERROR);
					return;
				}
				long longSalesFile = Long.parseLong(salesFile.get(1));

				Long saleAmount = branchSales.get(salesFile.get(0)) + longSalesFile;

				//エラー処理2-2
				if(saleAmount >= 10000000000L){
					System.out.println(FILE_OVER);
					return;
				}

				branchSales.put(salesFile.get(0), saleAmount);
			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return ;
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return ;
					}
				}
			}
		}
		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}
	}
	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;
		try {
			File file = new File(path, fileName);
			//ファイルの存在を確認 エラー処理1-1
			if(!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}

			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				String[]items = line.split(",");//(処理内容1-2)
				if((items.length != 2) || (!items[0].matches("^[0-9]{3}$"))){
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}

				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);
			}
		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;
		try {
			//新しくファイルを作り、pathとfilenameを指定。
			//変数bwに代入
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			//変数keyにbranchNamesのkeyすべてを代入
			//writeメソッドで支店番号、名前、金額を出力
			//newLineメソッドで改行

			for (String key : branchNames.keySet()) {
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				bw.newLine();
			}
		} catch(IOException e) {
			// 例外が発生した時の処理
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// 必ず行う処理
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}
}
