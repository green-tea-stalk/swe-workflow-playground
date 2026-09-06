package com.example.bulletinboard.exception;

/**
 * データベースへの永続化操作または問い合わせ失敗時にスローされるドメイン例外。
 * 根本原因となるJDBCまたはデータアクセス例外をラップして伝播する。
 */
public class DatabaseAccessException extends BulletinBoardException {

    /**
     * エラーメッセージおよび原因となった元例外を指定して例外を生成する。
     *
     * @param message エラー詳細メッセージ
     * @param cause 原因例外
     */
    public DatabaseAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
