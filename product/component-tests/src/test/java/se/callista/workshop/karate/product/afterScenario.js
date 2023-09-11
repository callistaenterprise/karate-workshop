function() {
  product_db.update('delete from product where id > 100');
  replenish_queue.close();
  resetMock(inventory_mock_url);
}